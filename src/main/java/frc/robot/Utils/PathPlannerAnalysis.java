package frc.robot.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.RobotBase;

/**
 * Scans the deployed PathPlanner folder and produces the data the "Autos"
 * dashboard tab renders: path geometry, event markers, mechanism spans, and
 * the conflicts that fall out of replaying those markers against a model of
 * the command scheduler.
 *
 * <p>This is the Java twin of {@code tools/pathplanner_audit.py}. The two
 * share no code, so the {@link #TRIGGERS} table below must be kept in sync
 * with both {@code RobotContainer}'s EventTrigger bindings and the Python
 * tool's own table.
 *
 * <p>Output is written as JSON to the WildBoard dynamic directory, which the
 * dashboard's HTTP server already serves at {@code /dynamic/}.
 */
public final class PathPlannerAnalysis {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** 2026 field extents in metres. */
    public static final double FIELD_X = 17.548;
    public static final double FIELD_Y = 8.052;

    /**
     * Mirror of the EventTrigger bindings in RobotContainer.
     *
     * <p><b>VERIFY THIS BEFORE TRUSTING THE ANALYSIS IN THIS REPO.</b> This
     * table was carried over from 2026-Robot. As of the port, this repo binds
     * no EventTriggers at all and none of the commands named below exist here,
     * so it is a template, not a description of this robot.
     *
     * <p>When you add triggers to {@code RobotContainer}, make this table match
     * them: the schedule kind ({@code onTrue} / {@code toggleOnTrue}), the
     * command's {@code addRequirements(...)} subsystems, and whether it has an
     * {@code isFinished()}. Those last two are what make the cancellation and
     * shot-parity analysis correct — a wrong entry produces confidently wrong
     * findings. Keep {@code tools/pathplanner_audit.py}'s TRIGGERS in sync too.
     */
    private record Trigger(String kind, String cmd, Set<String> reqs, boolean forever) {}

    private static final Map<String, Trigger> TRIGGERS = Map.of(
        "Intake",        new Trigger("onTrue", "AutoIntakeExtend",
                                     Set.of("intake", "intakeExtension"), false),
        "IntakeRetract", new Trigger("onTrue", "AutoIntakeRetract",
                                     Set.of("intake", "intakeExtension"), false),
        "StopIntake",    new Trigger("onTrue", "AutoIntakeStop",
                                     Set.of("intake"), false),
        "Shoot",         new Trigger("onTrue", "AutoStartIndexCommand",
                                     Set.of("transfer", "indexer"), false),
        "ShootStop",     new Trigger("onTrue", "AutoStopIndexCommand",
                                     Set.of("transfer", "indexer"), false),
        "AimAndShoot",   new Trigger("toggleOnTrue", "AimAndShootCommand",
                                     Set.of("shooter", "transfer", "indexer"), true));

    private static final String[][] ZONES = {
        {"LT-", "Left Trench"}, {"RT-", "Right Trench"}, {"LB-", "Left Bump"},
        {"RB-", "Right Bump"}, {"OUT-", "Outpost"}, {"CTR-", "Center / Mid"},
        {"DEP-", "Depot"}, {"S8-", "Shoot 8"}, {"45-", "45° start"},
        {"BUMP-", "Bump traverse"}, {"ZZ-", "Test / dev"}};

    private PathPlannerAnalysis() {}

    private static String zoneOf(String name) {
        for (String[] z : ZONES) {
            if (name.startsWith(z[0])) return z[1];
        }
        return "Unprefixed";
    }

    public static List<String> zoneLabels() {
        List<String> out = new ArrayList<>();
        for (String[] z : ZONES) out.add(z[1]);
        out.add("Unprefixed");
        return out;
    }

    // ---------------------------------------------------------------- paths

    private static Map<String, JsonNode> loadDir(File dir, String ext) {
        Map<String, JsonNode> out = new TreeMap<>();
        File[] files = dir.listFiles((d, n) -> n.endsWith(ext));
        if (files == null) return out;
        Arrays.sort(files);
        for (File f : files) {
            try {
                out.put(f.getName().substring(0, f.getName().length() - ext.length()),
                        MAPPER.readTree(f));
            } catch (IOException e) {
                System.err.println("[AutoTools] could not read " + f + ": " + e.getMessage());
            }
        }
        return out;
    }

    /** Ordered path names and auto-level NamedCommand nodes, depth first. */
    private static void walkAuto(JsonNode node, List<String> paths, List<String> named) {
        if (node == null || !node.isObject()) return;
        String type = node.path("type").asText("");
        JsonNode data = node.path("data");
        if (type.equals("path") && data.hasNonNull("pathName")) {
            paths.add(data.get("pathName").asText());
        }
        if (type.equals("named") && data.hasNonNull("name")) {
            named.add(data.get("name").asText());
        }
        for (JsonNode child : data.path("commands")) walkAuto(child, paths, named);
        if (data.hasNonNull("command")) walkAuto(data.get("command"), paths, named);
    }

    private record Marker(double pos, String name, boolean embedded) {}

    private static List<Marker> markersOf(JsonNode path) {
        List<Marker> out = new ArrayList<>();
        for (JsonNode m : path.path("eventMarkers")) {
            String name = m.path("name").asText("");
            if (name.isEmpty()) continue;
            out.add(new Marker(m.path("waypointRelativePos").asDouble(0.0), name,
                               m.hasNonNull("command")));
        }
        out.sort((a, b) -> Double.compare(a.pos, b.pos));
        return out;
    }

    private static int spanOf(JsonNode path) {
        return Math.max(path.path("waypoints").size() - 1, 1);
    }

    // ---------------------------------------------------------------- build

    /** Analyse everything and return the structure the dashboard renders. */
    public static Map<String, Object> analyse() {
        File pp = new File(Filesystem.getDeployDirectory(), "pathplanner");
        Map<String, JsonNode> paths = loadDir(new File(pp, "paths"), ".path");
        Map<String, JsonNode> autos = loadDir(new File(pp, "autos"), ".auto");

        if (paths.isEmpty() || autos.isEmpty()) {
            System.err.println("[AutoTools] WARNING: read " + paths.size() + " paths and "
                    + autos.size() + " autos from " + pp.getAbsolutePath()
                    + " — if that is unexpected, check that paths/ and autos/ still exist there");
        }

        Map<String, List<String>> pathUsers = new HashMap<>();
        Map<String, Object> autoOut = new LinkedHashMap<>();

        for (Map.Entry<String, JsonNode> e : autos.entrySet()) {
            String aName = e.getKey();
            List<String> seq = new ArrayList<>();
            List<String> named = new ArrayList<>();
            walkAuto(e.getValue().path("command"), seq, named);

            List<Map<String, Object>> segs = new ArrayList<>();
            List<Map<String, Object>> events = new ArrayList<>();
            List<Map<String, Object>> issues = new ArrayList<>();
            double offset = 0;

            for (String pName : seq) {
                pathUsers.computeIfAbsent(pName, k -> new ArrayList<>()).add(aName);
                JsonNode pj = paths.get(pName);
                if (pj == null) {
                    issues.add(issue("err", "references missing path '" + pName + "'"));
                    continue;
                }
                int span = spanOf(pj);
                Map<String, Object> sg = new LinkedHashMap<>();
                sg.put("n", pName);
                sg.put("s", offset);
                sg.put("e", offset + span);
                segs.add(sg);
                for (Marker m : markersOf(pj)) {
                    Map<String, Object> evm = new LinkedHashMap<>();
                    evm.put("g", offset + m.pos);
                    evm.put("l", m.pos);
                    evm.put("p", pName);
                    evm.put("n", m.name);
                    events.add(evm);
                }
                offset += span;
            }
            double total = offset == 0 ? 1 : offset;
            events.sort((a, b) -> Double.compare(num(a.get("g")), num(b.get("g"))));

            for (String n : named) {
                issues.add(issue("err", "auto-level NamedCommand '" + n
                        + "': NamedCommands.registerCommand is never called, so this auto fails to load"));
            }

            // --- replay the markers -------------------------------------
            List<Map<String, Object>> intake = new ArrayList<>();
            List<Map<String, Object>> shoot = new ArrayList<>();
            Double openIntake = null;
            Double openShot = null;
            int shotN = 0;
            boolean intakeDown = false;

            for (Map<String, Object> ev : events) {
                String n = (String) ev.get("n");
                double g = num(ev.get("g"));
                String where = ev.get("p") + " @" + fmt(num(ev.get("l")));
                if (!TRIGGERS.containsKey(n)) {
                    issues.add(issue("err", "[" + where + "] '" + n + "' is not bound in RobotContainer"));
                    continue;
                }
                switch (n) {
                    case "AimAndShoot" -> {
                        if (openShot != null) {
                            shoot.add(span(openShot, g, shotN, false));
                            openShot = null;
                        } else {
                            shotN++;
                            openShot = g;
                        }
                    }
                    case "Shoot", "ShootStop" -> {
                        if (openShot != null) {
                            shoot.add(span(openShot, g, shotN, false));
                            issues.add(issue("err", "[" + where + "] '" + n
                                    + "' requires transfer+indexer and CANCELS shot #" + shotN
                                    + " — the toggle flips off with no marker, inverting"
                                    + " AimAndShoot parity for the rest of the auto"));
                            openShot = null;
                        }
                    }
                    case "Intake" -> {
                        if (intakeDown) {
                            issues.add(issue("warn", "[" + where + "] 'Intake' fired while already down"));
                        } else {
                            openIntake = g;
                        }
                        intakeDown = true;
                    }
                    case "IntakeRetract", "StopIntake" -> {
                        if (!intakeDown) {
                            issues.add(issue("warn", "[" + where + "] '" + n + "' but intake was never down"));
                        } else {
                            intake.add(span(openIntake, g, 0, false));
                            openIntake = null;
                        }
                        intakeDown = false;
                    }
                    default -> { }
                }
            }
            if (openIntake != null) {
                intake.add(span(openIntake, total, 0, true));
                issues.add(issue("warn", "ends with intake still down"));
            }
            if (openShot != null) {
                shoot.add(span(openShot, total, shotN, true));
                issues.add(issue("warn", "ends with shot #" + shotN
                        + " still running (odd AimAndShoot count) — shooter carries into teleop"));
            }

            int errs = 0, warns = 0;
            for (Map<String, Object> i : issues) {
                if ("err".equals(i.get("lv"))) errs++; else warns++;
            }

            Map<String, Object> a = new LinkedHashMap<>();
            a.put("zone", zoneOf(aName));
            a.put("paths", seq);
            a.put("named", named);
            a.put("segs", segs);
            a.put("ev", events);
            a.put("intake", intake);
            a.put("shoot", shoot);
            a.put("shots", shotN);
            a.put("total", total);
            a.put("issues", issues);
            a.put("errors", errs);
            a.put("warns", warns);
            a.put("loadable", named.isEmpty() && errs == 0);
            autoOut.put(aName, a);
        }

        // --- paths ------------------------------------------------------
        Map<String, Object> pathOut = new LinkedHashMap<>();
        for (Map.Entry<String, JsonNode> e : paths.entrySet()) {
            String pName = e.getKey();
            JsonNode pj = e.getValue();
            List<String> users = pathUsers.getOrDefault(pName, List.of());
            Set<String> uniq = new HashSet<>(users);

            List<Map<String, Object>> wps = new ArrayList<>();
            for (JsonNode w : pj.path("waypoints")) {
                Map<String, Object> o = new LinkedHashMap<>();
                o.put("ax", w.path("anchor").path("x").asDouble());
                o.put("ay", w.path("anchor").path("y").asDouble());
                if (w.hasNonNull("nextControl")) {
                    o.put("ncx", w.get("nextControl").path("x").asDouble());
                    o.put("ncy", w.get("nextControl").path("y").asDouble());
                }
                if (w.hasNonNull("prevControl")) {
                    o.put("pcx", w.get("prevControl").path("x").asDouble());
                    o.put("pcy", w.get("prevControl").path("y").asDouble());
                }
                wps.add(o);
            }

            List<Map<String, Object>> mk = new ArrayList<>();
            for (Marker m : markersOf(pj)) {
                Map<String, Object> mm = new LinkedHashMap<>();
                mm.put("p", m.pos);
                mm.put("n", m.name);
                mm.put("emb", m.embedded);
                mk.add(mm);
            }

            // rotation track: start -> targets -> end, all in local relpos
            List<double[]> rot = new ArrayList<>();
            int span = spanOf(pj);
            if (pj.path("idealStartingState").hasNonNull("rotation")) {
                rot.add(new double[]{0, pj.path("idealStartingState").path("rotation").asDouble()});
            }
            for (JsonNode t : pj.path("rotationTargets")) {
                rot.add(new double[]{
                    Math.max(0, Math.min(t.path("waypointRelativePos").asDouble(), span)),
                    t.path("rotationDegrees").asDouble()});
            }
            if (pj.path("goalEndState").hasNonNull("rotation")) {
                rot.add(new double[]{span, pj.path("goalEndState").path("rotation").asDouble()});
            }
            rot.sort((x, y) -> Double.compare(x[0], y[0]));

            Map<String, Object> p = new LinkedHashMap<>();
            p.put("zone", zoneOf(pName));
            p.put("wps", wps);
            p.put("mk", mk);
            p.put("rot", rot);
            p.put("span", span);
            List<String> sortedUsers = new ArrayList<>(uniq);
            sortedUsers.sort(String::compareTo);
            p.put("autos", sortedUsers);
            p.put("shared", uniq.size() > 1);
            p.put("orphan", uniq.isEmpty());
            pathOut.put(pName, p);
        }

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("field", Map.of("x", FIELD_X, "y", FIELD_Y));
        root.put("robot", robotSpec(pp));
        root.put("zones", zoneLabels());
        root.put("paths", pathOut);
        root.put("autos", autoOut);
        root.put("built", System.currentTimeMillis());
        return root;
    }

    private static Map<String, Object> issue(String level, String text) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("lv", level);
        m.put("t", text);
        return m;
    }

    private static double num(Object o) {
        return o instanceof Number ? ((Number) o).doubleValue() : 0.0;
    }

    private static Map<String, Object> span(double a, double b, int n, boolean open) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("a", a);
        m.put("b", b);
        if (n > 0) m.put("n", n);
        m.put("open", open);
        return m;
    }

    private static String fmt(double v) {
        return String.format("%.2f", v);
    }

    /** Bumper box plus the robotFeatures drawing straight out of settings.json. */
    private static Map<String, Object> robotSpec(File pp) {
        Map<String, Object> spec = new LinkedHashMap<>();
        spec.put("w", 0.85);
        spec.put("l", 0.85);
        spec.put("ox", 0.0);
        spec.put("oy", 0.0);
        List<Object> features = new ArrayList<>();
        spec.put("features", features);
        try {
            JsonNode s = MAPPER.readTree(new File(pp, "settings.json"));
            spec.put("w", s.path("robotWidth").asDouble(0.85));
            spec.put("l", s.path("robotLength").asDouble(0.85));
            spec.put("ox", s.path("bumperOffsetX").asDouble(0.0));
            spec.put("oy", s.path("bumperOffsetY").asDouble(0.0));
            for (JsonNode raw : s.path("robotFeatures")) {
                // each entry is a JSON *string* containing an object
                features.add(MAPPER.readTree(raw.asText()));
            }
        } catch (Exception e) {
            System.err.println("[AutoTools] settings.json unreadable: " + e.getMessage());
        }
        return spec;
    }

    // --------------------------------------------------------------- output

    /**
     * Cheap fingerprint of the pathplanner folder: file count, names and
     * modified times. Poll this to notice edits made while the code is running
     * (PathPlanner writes straight into the deploy folder in simulation).
     */
    public static long folderStamp() {
        File pp = new File(Filesystem.getDeployDirectory(), "pathplanner");
        long h = 1125899906842597L;
        for (String sub : new String[]{"paths", "autos"}) {
            File[] files = new File(pp, sub).listFiles();
            if (files == null) continue;
            Arrays.sort(files);
            for (File f : files) {
                h = 31 * h + f.getName().hashCode();
                h = 31 * h + f.lastModified();
            }
        }
        return h;
    }

    /** Where the WildBoard HTTP server serves /dynamic/ from. */
    public static File dynamicDir() {
        return RobotBase.isSimulation()
                ? new File(Filesystem.getOperatingDirectory(), "sim/home/frontend-public/dynamic")
                : new File("/home/lvuser/WildBoard/frontend-public/dynamic");
    }

    /** What {@link #writeJson()} found. */
    public record Result(List<String> unloadable, Map<String, Integer> warnCounts) {
        public static Result empty() {
            return new Result(List.of(), Map.of());
        }
    }

    /**
     * Analyse and write {@code /dynamic/autoanalysis.json}.
     *
     * @return which autos will not load, and how many warnings each auto has.
     */
    public static Result writeJson() {
        List<String> broken = new ArrayList<>();
        Map<String, Integer> warnCounts = new HashMap<>();
        try {
            Map<String, Object> data = analyse();

            @SuppressWarnings("unchecked")
            Map<String, Object> autos = (Map<String, Object>) data.get("autos");
            for (Map.Entry<String, Object> e : autos.entrySet()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> a = (Map<String, Object>) e.getValue();
                if (!Boolean.TRUE.equals(a.get("loadable"))) broken.add(e.getKey());
                warnCounts.put(e.getKey(), (int) num(a.get("warns")));
            }

            File dir = dynamicDir();
            dir.mkdirs();
            File out = new File(dir, "autoanalysis.json");
            Files.writeString(out.toPath(), MAPPER.writeValueAsString(data));
            System.out.println("[AutoTools] wrote " + out.getAbsolutePath()
                    + "  (" + ((Map<?, ?>) data.get("paths")).size() + " paths, "
                    + autos.size() + " autos, " + broken.size() + " unloadable)");
        } catch (Exception e) {
            System.err.println("[AutoTools] analysis failed: " + e.getMessage());
            e.printStackTrace();
        }
        return new Result(broken, warnCounts);
    }
}
