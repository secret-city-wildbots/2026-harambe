# Porting the PathPlanner tooling + Autos tab to another repo

Everything added in this work, and the exact order to apply it. Written after
porting from `MapleSim Test` → `2026-Robot`, so the gotchas are real ones.

Throughout: **SRC** = the repo you are copying from, **DST** = the repo you are
copying into.

---

## ⚠ Read this first

**Close PathPlanner before you rename anything.** PathPlanner keeps the project
in memory. If you rename files underneath it, it can write its stale view back
over the folder — on the 2026-Robot port it deleted `paths/`, `autos/` and
`navgrid.json` outright, leaving only `settings.json`. It was recoverable from
git, but only because it was committed.

**Commit DST before you start.** The rename touches 100+ files. A clean
starting commit is your undo.

---

## Part 1 — Files to copy verbatim (new files, nothing to merge)

### Tooling

| File | What it is |
|---|---|
| `tools/pathplanner_audit.py` | The analyser. Everything else imports from it. **Copy this first.** |
| `tools/pathplanner_visualize.py` | Builds `pathplanner_view.html` (field + playback) |
| `tools/pathplanner_map.py` | Builds `pathplanner_map.html` + `PATHPLANNER_MAP.md` |
| `tools/pathplanner_rename.py` | The rename mapping + reference rewriter |
| `tools/view_template.html` | HTML/JS for the visualiser |
| `tools/map_template.html` | HTML/JS for the map |
| `tools/README.md` | How to use all of it |
| `tools/audit.ahk` | AutoHotkey v2 hotkeys (F9 / F10 / Ctrl+F9) |
| `tools/audit-ahk-v1.ahk` | Same, for AutoHotkey v1 |

### Launchers

**Note for this repo:** everything below lives under `tools/`, not the repo
root — launchers, the field image, these docs and the generated HTML. The repo
root only keeps `esbuild.exe` (FrontendBuilder requires it there) and
`pathplanner_archive/`. The `.bat` files `cd /d "%~dp0.."` so they still run
from the repo root, and the Python scripts write their generated output beside
themselves. `MapleSim Test` and `2026-Robot` still have the launchers at the
repo root.

`tools\audit.bat` · `tools\audit-all.bat` · `tools\audit-watch.bat` · `tools\view.bat` ·
`tools\view-once.bat` · `tools\map.bat`

They all use `%~dp0`, so they work unchanged in any repo.

### Field image

| File | Why two copies |
|---|---|
| `field2026.png` (repo root) | 2400px, used by the standalone `pathplanner_view.html` via a relative path |
| `src/main/deploy/WildBoard/frontend/public/field2026.png` | Full 6133px, served by the dashboard at `/field2026.png` |

### Dashboard — new files

| File |
|---|
| `src/main/java/frc/robot/Utils/PathPlannerAnalysis.java` |
| `src/main/java/frc/robot/WildBoard/Panels/AutoTools.java` |
| `src/main/deploy/WildBoard/frontend/src/panels/AutoTools.tsx` |

### Docs

`PATHPLANNER_NAMING.md` (the full rename mapping) ·
`pathplanner_archive/README.md` (explains the archive folder) ·
`PORTING.md` (this file)

---

## Part 2 — Files to overwrite (they exist in DST, replace them wholesale)

| File | Was → Is |
|---|---|
| `src/main/java/frc/robot/WildBoard/Panels/AutoChooser.java` | free-text/dropdown input → display-only armed-auto readout |
| `src/main/deploy/WildBoard/frontend/src/panels/AutoChooser.tsx` | same |

**Breaking API change:** the constructor went from `new AutoChooser(String[])`
with `.onChange(...)` to **`new AutoChooser()`** with `.setArmed(name, warns)`.
Every call site must be updated — see Part 3.

---

## Part 3 — Hand edits (4 files, 10 edits)

These files differ between repos, so **do not copy them.** Make the edits.

### `src/main/java/frc/robot/Robot.java` — 1 edit

In `robotPeriodic()`, uncomment the dashboard update:

```java
//m_robotContainer.dashboard.update();     ← was commented out
m_robotContainer.dashboard.update();       ← uncomment it
```

**This one matters most.** Without it no panel `update()` runs and no message
is ever flushed to the browser, so the Autos tab silently cannot arm, rescan,
or report what is armed. It was probably commented out because
`Dashboard.update()` used to crash in simulation — which the next edit fixes.

### `src/main/java/frc/robot/Dashboard.java` — 5 edits

1. **Add two panel fields** next to the other `final ... WB*` declarations:
   ```java
   final AutoTools WBautoTools;
   final AutoChooser WBautoChooser;
   ```

2. **Construct the chooser early**, right after `WBfieldMap = new FieldMap();`
   (it must exist before the TeleOp tab is built):
   ```java
   WBautoChooser = new AutoChooser();
   ```

3. **In the TeleOp tab**, replace the whole
   `new AutoChooser(new String[]{...}).onChange(...)` expression with just
   `WBautoChooser`. Delete the hardcoded auto-name list — after a rename those
   names no longer exist and selecting one throws.

4. **After the Subsystems tab**, add the Autos tab. Use a local variable so the
   lambda doesn't reference the blank final field:
   ```java
   AutoTools tools = new AutoTools();
   tools.onArm((String name) -> {
       autoChosen.accept(new PathPlannerAuto(name));
       WBautoChooser.setArmed(name, tools.getWarnCount(name) > 0);
   });
   WBautoTools = tools;

   dashboard.addTab(new Tab()
           .setTitle("Autos")
           .addChild(new Col(12).addChild(WBautoTools)));
   ```

5. **Split `update()`** so telemetry can't kill the flush, and fix the alliance
   crash. Rename the existing body to `updateTelemetry()`, change
   `DriverStation.getAlliance().get()` → `.orElse(Alliance.Blue)`, **delete the
   `dashboard.update();` from the end of the old body**, and add:
   ```java
   public void update() {
       try { updateTelemetry(); }
       catch (Exception e) {
           if (!telemetryFaulted) { telemetryFaulted = true;
               System.err.println("[Dashboard] telemetry update failed: " + e);
               e.printStackTrace(); }
       }
       dashboard.update();
   }
   private boolean telemetryFaulted = false;
   ```

### `src/main/java/frc/robot/WildBoard/Server.java` — 3 edits

All inside `StaticFileHandler.handle()`.

1. **Strip the context prefix** before resolving. This is the actual bug: a
   handler mounted at `/dynamic/` still receives `/dynamic/foo.json`, so it
   looked one directory too deep and 404'd everything under `/dynamic/`.
   ```java
   String context = exchange.getHttpContext().getPath();
   if (!"/".equals(context) && requestPath.startsWith(context)) {
       requestPath = "/" + requestPath.substring(context.length());
   }
   if (requestPath.equals("/") || requestPath.isEmpty())
       requestPath = "/index.html";
   ```

2. **Delete the `if (requestPath.equals("/dynamic/index.js")) { ... }` block.**
   It only existed to work around edit 1, and it wrote its response then *fell
   through* into the 404 branch, calling `sendResponseHeaders` twice.

3. **Add a JSON mime type** to the chain:
   ```java
   } else if (filePath.toString().endsWith(".json")) {
       mimeType = "application/json";
   } else {
   ```

### `src/main/java/frc/robot/Commands/SysCheckSequence.java` — 1 edit

Only if you apply the renames — the auto name is hardcoded:

```java
new PathPlannerAuto("SysCheck")   →   new PathPlannerAuto("ZZ-SysCheck")
```

Find any others with:
```
grep -rn 'PathPlannerAuto("' src/main/java
```

---

## Part 4 — `esbuild.exe` must exist in the DST repo root

**This one is easy to miss and it fails silently.** `*.exe` is gitignored, so
`esbuild.exe` never comes with a clone. In simulation `FrontendBuilder` runs
`<repo root>/esbuild.exe` to bundle the frontend; if it is absent the build
throws, the exception is caught and printed, and **`index.js` is simply never
rebuilt.**

The symptom is nasty: `index.tsx` regenerates with your new tab in it, the Java
side works, `autoanalysis.json` gets written — but the browser keeps serving the
*old* `index.js`, so your new panels are nowhere to be seen and it looks like
the port didn't happen.

```
copy <a repo that has it>\esbuild.exe   <DST>\esbuild.exe
```

Check for it before you conclude anything is broken:

```
dir esbuild.exe
```

Then confirm the bundle is actually fresh after a sim run — `index.js` should
be *newer* than your panel files:

```
dir sim\home\frontend-public\dynamic\index.js
dir src\main\deploy\WildBoard\frontend\src\panels\AutoTools.tsx
```

---

## Part 5 — Retarget the AutoHotkey scripts

`tools/audit.ahk` and `tools/audit-ahk-v1.ahk` hardcode the repo path. Update
the `REPO :=` line in both:

```autohotkey
REPO := "C:\Users\wildr\Programming\Robotics\FRC-4265\<DST folder>"
```

---

## Part 6 — Do NOT copy these (generated output)

Regenerate them in DST instead; copying them means DST shows SRC's data.

`PATHPLANNER_AUDIT.txt` · `PATHPLANNER_MAP.md` · `pathplanner_view.html` ·
`pathplanner_map.html` · `sim/home/**` · anything under `build/`

---

## Part 7 — Migrating the paths/autos themselves

**Order matters.** Archive first, or the rename pre-flight refuses to run
because the orphans have no mapping.

```bash
# 0. CLOSE PATHPLANNER. Commit DST.
git add -A && git commit -m "before pathplanner rename"

# 1. Archive orphans (paths no auto references)
mkdir -p pathplanner_archive/paths
python tools/pathplanner_audit.py --orphans > orphans.txt
#    move each listed <name> from src/main/deploy/pathplanner/paths/<name>.path
#    to pathplanner_archive/paths/<name>.path

# 2. Rename — dry run first, it refuses to apply if anything is unmapped
python tools/pathplanner_rename.py --dry-run
python tools/pathplanner_rename.py --apply

# 3. Remap Shoot/ShootStop markers to AimAndShoot (optional, changes behaviour)
#    In each affected .path, set "name": "AimAndShoot" and "command": null.
#    Find them with:
python tools/pathplanner_audit.py | grep "CANCELS the shot"

# 4. Update hardcoded auto names in Java (Part 3, SysCheckSequence)

# 5. Verify — all four of these must be zero / empty
python tools/pathplanner_audit.py --orphans          # expect nothing
python tools/pathplanner_audit.py | grep "references missing path"
python tools/pathplanner_audit.py | grep "CANCELS the shot"
python tools/pathplanner_rename.py --dry-run         # expect 0 renames (idempotent)

# 6. Regenerate the views and commit
python tools/pathplanner_map.py
python tools/pathplanner_visualize.py
python tools/pathplanner_audit.py > PATHPLANNER_AUDIT.txt
git add -A && git commit -m "pathplanner rename + tooling"
```

If DST has paths SRC doesn't (or vice versa), the rename script reports them as
**notes** and skips — that's fine. It only refuses to run on real hazards: a
name collision, or a file left on disk with no mapping.

---

## Part 8 — Keep these in sync by hand

Four copies of the same trigger table now exist. If you add or rebind an
`EventTrigger` in `RobotContainer`, update **all** of these:

- `RobotContainer.java` (the source of truth)
- `tools/pathplanner_audit.py` → `TRIGGERS`
- `src/main/java/frc/robot/Utils/PathPlannerAnalysis.java` → `TRIGGERS`
- …and the same two files in the other repo

Each entry needs the command's `addRequirements(...)` subsystems and whether it
has an `isFinished()`. Those two facts are what make the cancellation analysis
work.

Field dimensions are also duplicated — `FIELD_X` / `FIELD_Y` in both
`PathPlannerAnalysis.java` and `tools/pathplanner_visualize.py`.

---

## Part 9 — Verifying the port without deploying

```bash
./gradlew build                 # Java compiles (needs JDK 17)
./gradlew simulateJava          # then open localhost:5804
```

In the browser: the **Autos** tab should show `N paths · N autos · N shared ·
N unloadable` in its header. If it says:

| Symptom | Cause |
|---|---|
| `could not load /dynamic/autoanalysis.json (404)` | Server.java edit 1 missing |
| `No autos found` | `paths/` or `autos/` is empty or missing — check PathPlanner didn't eat them |
| spins on `loading autos…` | stale cached `index.js` — hard-refresh (Ctrl+Shift+R) |
| **Autos tab missing entirely / old panels still showing** | **`esbuild.exe` absent from the repo root — the bundle never rebuilt. See Part 4.** |
| nothing arms, Rescan does nothing, TeleOp never updates | `Robot.java` edit missing |
