# PathPlanner tools

- **`pathplanner_visualize.py`** — visual overlay: field route with intake/shooter
  spans drawn on it. Run `tools\view.bat`. **Start here.**
- **`pathplanner_map.py`** — path ↔ auto cross-reference. Run `tools\map.bat`.
  **Check this before editing a marker** — 30 of 67 paths are shared, and
  `RT-Base` alone feeds 11 autos.
- **`pathplanner_audit.py`** — text report of the same analysis, for one auto or all.
  Run `tools\audit.bat`.
- **`pathplanner_rename.py`** — bulk rename paths/autos, rewriting references.

---

# `pathplanner_map.py`

Builds `pathplanner_map.html` (interactive) and `PATHPLANNER_MAP.md` (static,
diffable in git). Run `tools\map.bat`.

**Event markers live in paths, not autos.** Editing one marker changes every
auto that runs that path — and 30 of 67 paths are shared. This is the tool that
tells you the blast radius before you touch anything.

Three columns: paths, autos, and a detail pane. Click a path and the autos using
it light up while the rest dim; click an auto and its paths light up in run
order. The detail pane shows markers, waypoint count, and — for a shared path —
an explicit warning listing every auto a marker edit would reach. Filter with
the search box, or narrow to **Shared only** / **Errored autos**.

Badges: a path shows how many autos use it (amber when >1, italic when orphaned);
an auto shows a red/amber/green dot for error/warning/clean.

---

# `pathplanner_visualize.py`

Builds `pathplanner_view.html` — the answer to "is intake down actually *over*
the stretch of path I meant?"

**Hotkey: F10 while PathPlanner is focused** (see `audit.ahk` below), or run
`tools\view-once.bat`.

**`tools\view.bat`** opens the page and then rebuilds it every time you save
in PathPlanner; press F5 in the browser to see the update. Keep it on a second
monitor next to PathPlanner.

The page **opens on the auto you most recently saved**, so F10 straight out of
PathPlanner lands on what you were just editing. The dropdown has the rest
(autos with errors are marked `●`). Four panels:

The robot is drawn from **`settings.json`** — the 0.8509 m bumper box plus every
entry in `robotFeatures` (frame outline, intake bar, intake rectangle, turret
circle, superstructure rectangle). Update the robot in PathPlanner and it
updates here on the next rebuild; nothing is hardcoded. The intake bar lights
green while the intake is down and the turret circle lights orange while the
shooter is running.

Heading comes from each path's `idealStartingState` → `rotationTargets` →
`goalEndState`, interpolated the short way round, so the robot **turns as it
turns in the auto**. A blue nose triangle marks the front.

**Playback** — Play walks a robot along the route while chips read out what
is happening *right there*: current path, position within it, intake down/up,
which shot is running, and the last and next markers. Scrub with the slider,
`Space` to play/pause, `←`/`→` to step (hold `Shift` for 1 m jumps). On the
field the robot grows a green ring while the intake is down and an orange ring
while the shooter is running; a blue playhead tracks the same spot on the bar
view below.

Playback moves at a **constant speed along the route** — it is not the real
trajectory timing, which would need PathPlanner's velocity and acceleration
profiles. It answers "what is the robot doing at this point on the field",
not "how many seconds in does this happen".

**Field** — the auto's full route drawn end to end. A thick green underlay marks
where the intake is down; an orange line marks where the shooter is running.
Dots are markers, hover for the name and position. White circle = start,
white square = end.

**Along the path** — the same spans as horizontal bars, with vertical dividers
showing where each path hands off to the next. This is the view that answers the
"is it over the right stretch" question: you can see a green bar ending halfway
through the path it was supposed to cover. A dashed bar means the span never
closed. Red vertical lines mark parity-breaking events.

**Findings** — the same errors the auditor reports.

Marker placement is exact, not approximate. `waypointRelativePos` indexes into
the path's cubic bezier segments (a marker at `1.81` is 81% along the segment
from waypoint 1 to waypoint 2), and the script evaluates that bezier directly.

`FIELD_X` / `FIELD_Y` at the top of the script are the field extents in metres —
adjust if the official 2026 numbers differ from what's there.

---

# `pathplanner_audit.py`

Static checker for PathPlanner autos. It expands each `.auto` into its full
ordered path sequence, replays the event markers, and models the WPILib
scheduler — so it can tell you what the robot will actually do without
running a simulation.

Its main job is **counting AimAndShoot parity for you.**

## The button

**One click:** run `tools\audit.bat` (repo root). It audits the *most recently saved
auto*, which is the one you have open in PathPlanner. Pin it to your taskbar
or make a desktop shortcut, then: edit in PathPlanner → Ctrl+S → click.

**Zero clicks:** run `tools\audit-watch.bat` once and leave the window open next to
PathPlanner. It re-runs itself every time you save. This is the better setup
if you have a second monitor.

**Hotkeys (AutoHotkey):** double-click `tools/audit.ahk`. A green **H** appears
in the system tray and these become live:

| Key | Does |
|---|---|
| `F9` | audit the auto you're looking at |
| `F10` | rebuild + open the visual overlay |
| `Ctrl+F9` | audit every auto with an error |

Those three only fire **while PathPlanner is the active window**, deliberately —
VS Code uses F9 for breakpoints and F10 for step-over, and scoping them avoids
the collision. From any window: `Ctrl+Alt+F9` and `Ctrl+Alt+F10` do the same
thing, `Ctrl+Alt+R` reloads the script, `Ctrl+Alt+I` prints the active window's
title and exe.

*Load it at login:* press `Win+R`, type `shell:startup`, drop a shortcut to
`audit.ahk` in the folder that opens.

*If it errors on load* you have AutoHotkey v1, not v2 — use
`tools/audit-ahk-v1.ahk` instead.

*If the scoped keys do nothing* but `Ctrl+Alt+F9` works, PathPlanner's window
title isn't matching. Focus PathPlanner, press `Ctrl+Alt+I`, and put the real
title in the `WinActive(...)` line.

## Command line

```
python tools/pathplanner_audit.py                  # audit everything
python tools/pathplanner_audit.py --last           # the auto you're looking at
python tools/pathplanner_audit.py --auto "LT-2Dip" # one auto by name
python tools/pathplanner_audit.py --problems       # only autos with errors
python tools/pathplanner_audit.py --timeline       # timelines for every auto
python tools/pathplanner_audit.py --watch          # re-run on every save
python tools/pathplanner_audit.py --orphans        # paths no auto uses
```

Exit code is `1` if any auto has an error, so it also works in CI or a
pre-commit hook.

## Reading the output

Auditing a single auto always prints the shot timeline:

```
L Trench 2 Dip
  3 paths, 8 markers, 3 shot(s), shooter RUNNING at end
  timeline:
    [L Trench Face Forwards @0.29] Intake
    [L Trench Face Forwards @0.59] AimAndShoot  ->  START shot #1
    [L Trench Face Forwards @1.81] AimAndShoot  ->  STOP  shot #1
    [L Trench over Bump @1.00]     AimAndShoot  ->  START shot #2
    ...
  X auto ends with shot #3 STILL RUNNING
```

`START` / `STOP` is the even/odd count you were doing in your head, resolved
across every path in the auto. The summary line tells you immediately whether
the shooter is left running when the auto ends.

### `X` — errors

| Finding | What it means |
|---|---|
| `auto ends with shot #N STILL RUNNING` | Odd number of `AimAndShoot` markers. `AimAndShootCommand` has no `isFinished()`, so it holds shooter/transfer/indexer into teleop. |
| `'Shoot'/'ShootStop' ... CANCELS the shot` | **The parity desync.** These commands require `transfer`+`indexer`, so scheduling one cancels a running `AimAndShootCommand`. WPILib's `toggleOnTrue` checks `isScheduled()`, so the toggle flips to OFF *without a marker* — and your next `AimAndShoot` starts a shot when you meant it to stop one. This is the bug you can't catch by counting. |
| `auto-level NamedCommand '...'` | `NamedCommands.registerCommand` is never called in this project. Any auto using a named-command node fails to load. |
| `marker past end of path` | `waypointRelativePos` exceeds the waypoint count — the marker never fires. |
| `trigger not bound in RobotContainer` | Typo'd or removed trigger name. |

### `~` — warnings

Intake extended twice in a row, retracted without being extended, or left
extended at the end of the auto. Usually harmless, sometimes a missing marker.

## Keeping it accurate

The `TRIGGERS` table at the top of the script mirrors the `EventTrigger`
bindings in `RobotContainer.java`. **If you add, rename, or rebind a trigger
there, update the table.** Each entry needs:

- `kind` — `onTrue` or `toggleOnTrue`
- `cmd` — the command class name
- `reqs` — its `addRequirements(...)` subsystems, which is how cancellation is detected
- `forever` — `True` if the command has no `isFinished()`

Getting `reqs` and `forever` right is what makes the cancellation analysis work.
