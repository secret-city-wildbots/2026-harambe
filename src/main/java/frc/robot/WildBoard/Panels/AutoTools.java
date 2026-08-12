package frc.robot.WildBoard.Panels;

import java.util.List;
import java.util.function.Consumer;

import edu.wpi.first.wpilibj.RobotBase;
import frc.robot.Utils.PathPlannerAnalysis;
import frc.robot.WildBoard.WBPanel;

/**
 * The "Autos" tab: a field visualiser and a path/auto cross-reference map.
 *
 * <p>The heavy data (every path's geometry, markers and analysis) is written
 * once to {@code /dynamic/autoanalysis.json} and fetched by the frontend, so
 * it never travels over the websocket. The socket carries only the armed auto
 * and rescan requests.
 *
 * <p>Messages from the frontend:
 * <ul>
 *   <li>{@code arm:<autoName>} — arm this auto for the match</li>
 *   <li>{@code rescan} — re-read the deploy folder and rewrite the JSON</li>
 *   <li>{@code hello} — ask what is currently armed (sent on page load)</li>
 * </ul>
 *
 * <p>Messages to the frontend: {@code armed:<name>} (empty name means nothing
 * is armed), {@code refused:<name>}, {@code rescanned}.
 *
 * <p>Incoming messages arrive on the websocket thread. They are parked in
 * volatile fields and acted on in {@link #update()}, which the WildBoard loop
 * calls on the robot thread — building a {@code PathPlannerAuto} off-thread
 * would race the command scheduler.
 */
public class AutoTools extends WBPanel {

    private Consumer<String> onArm = name -> { };

    /* Written by the analysis thread, read by the robot thread. */
    private volatile PathPlannerAnalysis.Result scan = PathPlannerAnalysis.Result.empty();
    private volatile boolean scanned = false;
    private volatile boolean scanning = false;
    private volatile boolean scanJustFinished = false;

    private volatile String armRequest = null;
    private volatile boolean rescanRequest = false;
    private volatile boolean helloRequest = false;

    private String armedAuto = "";
    private String lastBroadcast = null;

    /** Auto-rescan bookkeeping (simulation only — see update()). */
    private volatile long folderStamp = 0;
    private int pollCounter = 0;
    private static final int POLL_EVERY_LOOPS = 100;   // ~2 s at 20 ms

    public AutoTools() {
        this.usesML = true;
        this.setPanelName("AutoTools");
    }

    /** Called on the robot thread with the auto name when one is armed. */
    public AutoTools onArm(Consumer<String> handler) {
        this.onArm = handler;
        return this;
    }

    /** The auto currently armed, or "" if none. */
    public String getArmed() {
        return this.armedAuto;
    }

    /** Auto names that will not load — arming these is refused. */
    public List<String> getUnloadable() {
        return this.scan.unloadable();
    }

    /** Warning count the analysis found for an auto (0 if unknown). */
    public int getWarnCount(String name) {
        return this.scan.warnCounts().getOrDefault(name, 0);
    }

    @Override
    public void start() {
        // Deliberately does NOT analyse here.
        //
        // WildBoard.serverStart() calls every panel's start() BEFORE the HTTP
        // server begins listening. The analysis parses every .path and .auto
        // with Jackson and writes ~70 KB of JSON — fast on a laptop, slow on a
        // roboRIO — so doing it inline delayed, and could prevent, the
        // dashboard ever coming up on the real robot. The frontend fetches the
        // JSON over HTTP well after startup anyway, and shows "No autos found"
        // with a Rescan button if it arrives early.
        startScan();
    }

    /** Run the analysis on a background thread. No-op if one is already running. */
    private void startScan() {
        if (this.scanning) return;
        this.scanning = true;
        Thread t = new Thread(() -> {
            try {
                long stamp = PathPlannerAnalysis.folderStamp();
                PathPlannerAnalysis.Result result = PathPlannerAnalysis.writeJson();
                this.folderStamp = stamp;
                this.scan = result;
                this.scanned = true;
                this.scanJustFinished = true;
                if (!result.unloadable().isEmpty()) {
                    System.err.println("[AutoTools] these autos will NOT load: "
                            + String.join(", ", result.unloadable()));
                }
            } catch (Throwable e) {
                // Never let this kill startup or the robot program.
                System.err.println("[AutoTools] analysis thread failed: " + e);
                e.printStackTrace();
            } finally {
                this.scanning = false;
            }
        }, "AutoTools-Analysis");
        t.setDaemon(true);
        t.start();
    }

    @Override
    public void onMsg(String msg) {
        if (msg == null) return;
        if (msg.equals("hello")) {
            this.helloRequest = true;
        } else if (msg.equals("rescan")) {
            this.rescanRequest = true;
        } else if (msg.startsWith("arm:")) {
            this.armRequest = msg.substring(4).trim();
        }
    }

    @Override
    public void update() {
        if (this.ml == null) return;

        // In simulation PathPlanner writes straight into the deploy folder we
        // read, so poll for edits and rescan on our own. On a real robot the
        // deploy folder cannot change while the code runs, so don't waste the
        // directory listing.
        if (RobotBase.isSimulation() && ++this.pollCounter >= POLL_EVERY_LOOPS) {
            this.pollCounter = 0;
            long now = PathPlannerAnalysis.folderStamp();
            if (now != this.folderStamp) {
                this.folderStamp = now;
                this.rescanRequest = true;
                System.out.println("[AutoTools] pathplanner folder changed, rescanning");
            }
        }

        if (this.rescanRequest) {
            this.rescanRequest = false;
            startScan();          // background, same as at startup
        }

        // The analysis thread cannot touch the message queue safely, so it
        // raises a flag and the robot thread does the send.
        if (this.scanJustFinished) {
            this.scanJustFinished = false;
            this.ml.send("rescanned");
        }

        String req = this.armRequest;
        if (req != null) {
            this.armRequest = null;
            if (!req.isEmpty()) {
                // Until the first analysis lands we do not know which autos are
                // unloadable, so refuse rather than arm something that throws.
                if (!this.scanned) {
                    this.ml.send("refused:" + req + "|still analysing, try again in a moment");
                } else
                // Refuse anything the analysis says cannot be constructed,
                // rather than letting PathPlannerAuto throw during a match.
                if (this.scan.unloadable().contains(req)) {
                    System.err.println("[AutoTools] refused to arm '" + req
                            + "' — it has errors and would fail to load");
                    this.ml.send("refused:" + req + "|has errors, would fail to load");
                } else {
                    try {
                        this.onArm.accept(req);
                        this.armedAuto = req;
                        System.out.println("[AutoTools] armed auto: " + req);
                    } catch (Exception e) {
                        // Report the reason to the dashboard, not just "refused".
                        // A null autoChosen consumer in RobotContainer shows up
                        // here as a NullPointerException and is otherwise
                        // invisible from the UI.
                        String why = e.getClass().getSimpleName()
                                + (e.getMessage() == null ? "" : ": " + e.getMessage());
                        System.err.println("[AutoTools] failed to arm '" + req + "': " + why);
                        e.printStackTrace();
                        this.ml.send("refused:" + req + "|" + why);
                    }
                }
            }
        }

        // Broadcast the armed auto when it changes, or when a freshly loaded
        // page asks. Without the hello handshake a browser refresh would show
        // nothing armed even though the robot still has one.
        if (this.helloRequest || !this.armedAuto.equals(this.lastBroadcast)) {
            this.helloRequest = false;
            this.lastBroadcast = this.armedAuto;
            this.ml.send("armed:" + this.armedAuto);
        }
    }
}
