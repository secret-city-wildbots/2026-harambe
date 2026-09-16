# PathPlanner tools

- **`pathplanner_visualize.py`** — visual overlay: field route with intake/shooter
  spans drawn on it. Run `tools\view.bat`. **Start here.**
- **`pathplanner_map.py`** — path ↔ auto cross-reference. Run `tools\map.bat`.
  **Check this before editing a marker** — 30 of 67 paths are shared, and
  `RT-Base` alone feeds 11 autos.
- **`pathplanner_audit.py`** — text report of the same analysis, for one auto or all.
  Run `tools\audit.bat`.
- **`pathplanner_mirror.html`** — flip a path *or a whole auto* to the other side
  of the field. Double-click `tools\mirror.bat`, load the `pathplanner` folder,
  tick the autos you want, download.
- **`pathplanner_rename.py`** — bulk rename paths/autos, rewriting references.

---

# `pathplanner_mirror.html`

Left ↔ right mirror for `.path` **and `.auto`** files. A standalone page — no
Python, no build. Double-click **`tools\mirror.bat`** (or just open the `.html`).

Everything runs in the browser, so this ports straight into WildBoard later —
the `MIRROR CORE` block at the top of the file is DOM-free and lifts out as a
`.ts` module unchanged (`mirrorPath`, `mirrorAuto`, `mirrorName`, `autoPathRefs`,
`autoSteps`, `sampleAt`).

## The flow

1. **Load folder** → point it at `src\main\deploy\pathplanner`. All 66 paths and
   37 autos load as a *library*. Nothing is selected — this is context, so the
   tool knows which counterparts already exist.
2. **Tick the autos** (or paths) you want mirrored. Ticking an auto pulls in every
   path it runs; those path checkboxes lock, with a tooltip naming the auto.
3. **Download selected** → the `.auto` files and the `.path` files they need.

You can also just drop files on the page. Anything dropped or picked with
**Load files** arrives ticked, so dropping a single `.auto` is the one-shot
version of the whole flow — as long as the library is loaded, or the paths come
along in the same drop.

## What a path mirror changes

| | |
|---|---|
| positions | `y' = 8.052 - y`, on anchors and both control handles. `x` untouched. |
| headings | negated and normalized to `(-180, 180]` — `rotationTargets`, `idealStartingState`, `goalEndState`, `pointTowardsZones.rotationOffset` |
| `pointTowardsZones` | `fieldPosition.y` flipped too |
| everything else | copied verbatim — velocities, accelerations, constraint zones, event markers, `waypointRelativePos`, `reversed`, `folder` |

Mirroring twice returns the original to within 1e-12 m, and key order is preserved,
so a mirrored file diffs cleanly against its source.

Field width is the `field Y` box, default **8.052** to match `FIELD_Y` in
`pathplanner_visualize.py`. Change it in one place if the 2026 number differs.

## What an auto mirror changes

**Nothing but the path references.** An `.auto` holds no field geometry of its
own — the starting pose comes from the first path's `idealStartingState`, which
the path mirror already flipped. So mirroring an auto is repointing every `path`
node and leaving the rest alone: wait times, `resetOdom`, `choreoAuto`, `folder`
and named-command names all carry over untouched.

The walk is generic over the command tree, so `parallel` / `race` / `deadline`
groups and nested sequences mirror the same as a flat sequence — not just the
shape your autos happen to use today.

Named commands are **not** renamed. `Shoot` stays `Shoot`. If you ever add a
side-specific one you'll have to fix it by hand.

The **Sequence** table shows the whole auto step by step, `was` → `now`, with a
badge on each path saying whether it's a new file, a reused existing one, or not
loaded. The field preview draws the entire route end to end, original in grey and
mirrored in green, numbered in run order.

## Duplicates — the setting that matters for autos

Mirror `RT-2Dip` and it wants `LT-Base`, `LT-Dip1`, `LT-Base-Rev-OB-2`… and you
already hand-tuned two of those. The **duplicates** dropdown decides what happens:

- **reuse existing** (default) — if the mirrored name is already in the loaded
  library, no file is written and the mirrored auto points at *your* version.
  The row greys out with a `reuse` badge. This is almost always what you want:
  it's how a mirrored auto ends up running real paths instead of 6 near-duplicates.
- **write a copy** — mirror everything fresh. Rows that would overwrite an existing
  file are flagged red, so you can rename before downloading.

Either way the auto's `pathName` string is the same; only the set of files
written changes.

## Names

L/R tokens get swapped: `LT-Dip2 → RT-Dip2`, `LB-Plow-P1 → RB-Plow-P1`,
`45-Dip-FromLBump → 45-Dip-FromRBump`, `LT-FromRPlow → RT-FromLPlow`,
`LT-RDip1 → RT-LDip1`, `BUMP-Ride-LR → BUMP-Ride-RL`, `S8-RT-Dip → S8-LT-Dip`.
A name with no L/R in it (`CTR-Depot`, `OUT-Base`, `S8-Plow`) gets `-Mirror` and a
**suffix** badge, so you know to name it yourself. The name box is editable either
way, and editing it re-resolves everything downstream.

Because the library is loaded, `LT-Plow → RT-Plow` lights up red *"name exists"*
instead of you overwriting a hand-tuned file with a download.

## Linked waypoints — read this one

28 of the 66 paths have linked waypoints, and a link is what breaks a naive mirror:
keep `linkedName: "L Bump 45"` on a mirrored waypoint and PathPlanner snaps it back
to the original anchor, silently un-mirroring that point.

The **links** dropdown:

- **swap L/R** (default) — `L Bump 45 → R Bump 45`. If that anchor already exists
  in PathPlanner the waypoint snaps to *it* rather than the exact mirror, which is
  usually what you want on a real field. Links with no L/R counterpart (`Dip 1`,
  `Outpost`) are dropped.
- **unlink all** — safest. Every waypoint becomes a plain anchor at the mirrored spot.
- **keep** — for when you know what you're doing. Flagged in red.

Whatever the mode, the page lists exactly what happened to each linked waypoint —
on an auto, aggregated across every path it writes.

## Reading the preview

Original in grey, mirrored in green, over `field2026.png`. Dashed line down the
middle is the flip axis. White circle = start, hollow square = end, blue arrows
are headings (start, every rotation target, end), amber dots are event markers
placed by evaluating the bezier at their `waypointRelativePos` — the same exact
placement `pathplanner_visualize.py` uses. Numbered pips mark each path's start
in auto run order.

For a path, the waypoint table shows `was x, y → now x, y` and the link
before/after, plus a heading table. That's the verification pass: the sum of each
`was y` and `now y` should be the field width.

## Then

Downloads land in your browser's download folder. `.path` files go in
`src\main\deploy\pathplanner\paths\`, `.auto` files in `autos\`. Reopen
PathPlanner. `folder` is copied unchanged, so a mirrored path or auto shows up in
the same PathPlanner folder as its original; drag it where it belongs.

Then run `tools\audit.bat` on the new auto. Mirroring preserves marker order, so
if the original had clean shot parity the mirror does too — but run it anyway,
because a reused path may carry markers the mirrored side doesn't expect.
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
