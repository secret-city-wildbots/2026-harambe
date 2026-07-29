package frc.robot.WildBoard;

import frc.robot.WildBoard.Panels.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.Timer;

public class WildBoard {
    private Server server;
    private int PORT = 5804;
    private ArrayList<WBPanel> panels = new ArrayList<>();
    private ArrayList<WBPanel> updatePanels = new ArrayList<>();
    private ArrayList<WBPanel> updatePanelsTeleOp = new ArrayList<>();
    private ArrayList<Tab> tabs = new ArrayList<>();
    private Overrides overrides;
    private double lastTime_s;
    public static double loopTime_ms;

    public WildBoard() {}
    public WildBoard(int PORT) { this.PORT = PORT; }

    public void addPanel(WBPanel panel) { panels.add(panel); }
    public void addTab(Tab tab) { tabs.add(tab); }

    public List<Tab> getTabs() {
        return this.tabs;
    }

    public List<WBPanel> getPanels() {
        return this.panels;
    }

    public void start() {
        serverStart();
        clientBuild();
        this.server.start();
        lastTime_s = Timer.getTimestamp();
    }

    private void clientBuild() {
        File wildboardHome = RobotBase.isSimulation()
                ? new File(Filesystem.getOperatingDirectory(), "sim/home")
                : new File("/home/lvuser/WildBoard");

        File deployDir = Filesystem.getDeployDirectory();
        File frontendDir = new File(deployDir, "WildBoard/frontend");

        File indexStub = new File(frontendDir, "src/pages/indexStub.tsx");
        File indexFinalDir = new File(wildboardHome, "frontend/src/pages");
        File indexFinal = new File(indexFinalDir, "index.tsx");

        File indexLoader = new File(frontendDir, "src/pages/indexLoader.tsx");
        File indexLoaderFinal = new File(indexFinalDir, "indexLoader.tsx");

        try {
            String index = Files.readString(indexStub.toPath());
            String tabsStr = "";
            String sidepanels = "";
            String imports = "";

            for (Tab tab : this.tabs) {
                tabsStr += tab.generate();
                imports += tab.genImport() + "\n";
            }
            for (WBPanel panel : this.panels) {
                sidepanels += "<div class=\"column-item\" style=\"padding-bottom: 0;\">\r\n"
                        + panel.generate() + "\n</div>";
                imports += panel.genImport() + "\n";
            }

            index = index.replace("[TABS]", tabsStr)
                    .replace("[SIDEPANELS]", sidepanels)
                    .replace("[IMPORTS]", imports)
                    .replaceAll("\\[DEPLOY\\]", deployDir.getCanonicalPath().replaceAll("\\\\", "/"));

            indexFinalDir.mkdirs();
            Files.writeString(indexFinal.toPath(), index);

            Files.writeString(indexLoaderFinal.toPath(),
                    Files.readString(indexLoader.toPath())
                            .replaceAll("\\[DEPLOY\\]", deployDir.getCanonicalPath().replaceAll("\\\\", "/"))
                            .replaceAll("\\[HOME\\]", wildboardHome.getCanonicalPath().replaceAll("\\\\", "/")));

        } catch (IOException err) {
            System.err.println("Error during frontend construction:");
            err.printStackTrace();
        }

        FrontendBuilder.buildFrontend();
    }

    private void serverStart() {
        server = new Server(PORT);
        AtomicInteger mlId = new AtomicInteger(0);

        for (WBPanel wbPanel : panels) {
            if (wbPanel.usesML) {
                int id = mlId.getAndIncrement();
                wbPanel.assignML(new MessageLayer(server, id), id);
                this.updatePanels.add(wbPanel);
                this.updatePanelsTeleOp.add(wbPanel);
            }
            wbPanel.start();
        }

        for (Tab tab : tabs) {
            WBPanelUtils.traverse(tab, panel -> {
                if (panel.usesML) {
                    int id = mlId.getAndIncrement();
                    panel.assignML(new MessageLayer(server, id), id);
                    this.updatePanels.add(panel);
                    if ("TeleOp".equals(tab.getTitle())) this.updatePanelsTeleOp.add(panel);
                    if ("Overrides".equals(panel.getPanelName())) this.overrides = (Overrides) panel;
                }
                panel.start();
            });
        }
    }

    public void update() {
        if (!(overrides != null && overrides.compMode)) {
            for (WBPanel wbPanel : updatePanels) {
                wbPanel.update();
                wbPanel.ml.update();
            }
        } else {
            for (WBPanel wbPanel : updatePanelsTeleOp) {
                wbPanel.update();
                wbPanel.ml.update();
            }
        }
        server.ws.flush();
        double curTime_s = Timer.getTimestamp();
        loopTime_ms = Math.floor((curTime_s - lastTime_s) * 1000);
        lastTime_s = curTime_s;
    }
}