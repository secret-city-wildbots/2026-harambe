# PathPlanner Naming Convention — APPLIED 2026-07-25

## Why prefixes and not folders

PathPlanner's search box only filters the folder you're currently in. So folders
actively make discovery *worse*: a path you can't remember the folder for becomes
unfindable. A flat list with a leading zone prefix gives you both things you want:

- **Alphabetical sort clusters by zone** — all Left Trench paths sit together.
- **Typing the prefix filters** — `RT-` in the search box shows only Right Trench.

Keep `paths/` and `autos/` flat. Never create subfolders.

## Zone prefixes

| Prefix  | Zone / start                        |
|---------|-------------------------------------|
| `LT-`   | Left Trench                         |
| `RT-`   | Right Trench                        |
| `LB-`   | Left Bump                           |
| `RB-`   | Right Bump                          |
| `OUT-`  | Outpost                             |
| `CTR-`  | Center / Mid field                  |
| `DEP-`  | Depot (when Depot is the *origin*)  |
| `S8-`   | Shoot-8 opener                      |
| `45-`   | 45° start position                  |
| `ZZ-`   | Test / dev / scratch — sorts last   |

## Suffix conventions

- `-Rev` — same route driven in reverse
- `-OB` — over the bump (as opposed to around)
- `-Fwd` / `-Back` — robot heading, when two variants exist
- `-P1`, `-P2` — multi-segment split of one motion
- Drop `Copy of` entirely. If a copy is a real variant, name the variant
  (`-OB`, `-Fwd`); if it's an accident, it should be archived.

## Applied path renames (67 paths)

### Left Trench — `LT-`

| Current | Proposed |
|---|---|
| `L Trench` | `LT-Base` |
| `L Trench 45` | `LT-45` |
| `L Trench Back to Mid` | `LT-BackToMid` |
| `L Trench Dip 1` | `LT-Dip1` |
| `L Trench Dip 2` | `LT-Dip2` |
| `L Trench Dip 3 and Push` | `LT-Dip3Push` |
| `L Trench RDip 1` | `LT-RDip1` |
| `L Trench RDip 2` | `LT-RDip2` |
| `L Trench Face Forwards` | `LT-Base-Fwd` |
| `L Trench Reverse` | `LT-Base-Rev` |
| `L Trench over Bump` | `LT-Base-OB` |
| `L Trench + Depot + Pickup` | `LT-Depot-Pickup` |
| `L Trench to Depot to Mid` | `LT-Depot-Mid` |
| `L Trench + Plow` | `LT-Plow` |
| `L Trench from R Plow` | `LT-FromRPlow` |
| `L Trench Slow Lob` | `LT-SlowLob` |

### Right Trench — `RT-`

| Current | Proposed |
|---|---|
| `R Trench` | `RT-Base` |
| `R Trench Back to Mid` | `RT-BackToMid` |
| `R Trench Dip` | `RT-Dip1` |
| `Copy of R Trench Dip` | `RT-Dip2` |
| `R Trench Quarter` | `RT-Quarter` |
| `R Trench Quarter Reverse` | `RT-Quarter-Rev` |
| `R Trench Reverse` | `RT-Base-Rev` |
| `R Trench Reverse (over bump)` | `RT-Base-Rev-OB` |
| `Copy of R Trench Reverse (over bump)` | `RT-Base-Rev-OB-2` |
| `Front end R Trench Reverse` | `RT-Base-Rev-Fwd` |
| `R Trench + Shoot` | `RT-Shoot` |
| `R Trench Shoot` | *duplicate name — pick one, archive the other* |
| `R Trench + Plow` | `RT-Plow` |
| `R Trench from L Plow` | `RT-FromLPlow` |

### Left Bump — `LB-`

| Current | Proposed |
|---|---|
| `L Bump Dip` | `LB-Dip` |
| `L Bump 45 to Depot` | `LB-45-Depot` |
| `L Bump to Depot to Climb` | `LB-Depot-Climb` |
| `L Bump Plow P.1` | `LB-Plow-P1` |
| `L Bump Plow P.2` | `LB-Plow-P2` |

### Right Bump — `RB-`

| Current | Proposed |
|---|---|
| `R Bump to R Trench` | `RB-RTrench` |
| `R Bump to Outpost` | `RB-Outpost` |
| `R Bump into Outpost` | `RB-Outpost-In` |
| `R Bump to Outpost sideways` | `RB-Outpost-Side` |
| `R Bump Plow P.1` | `RB-Plow-P1` |
| `R Bump Plow P.2` | `RB-Plow-P2` |

### Outpost — `OUT-`

| Current | Proposed |
|---|---|
| `Outpost + Shoot` | `OUT-Shoot` |
| `Outpost Sweep` | `OUT-Sweep` |
| `Outpost to Mid` | `OUT-Mid` |
| `Outpost to Mid Dip` | `OUT-MidDip` |
| `Outpost to hub` | `OUT-Hub` |
| `Outpost to R Trench + Shoot` | `OUT-RTrench-Shoot` |
| `Sideways outpost to R Trench` | `OUT-Side-RTrench` |

### 45° start — `45-`

| Current | Proposed |
|---|---|
| `45 to L Bump` | `45-LBump` |
| `Copy of 45 to L Bump` | `45-LBump-2` |
| `45 to Mid` | `45-Mid` |
| `45 Dip from L Bump` | `45-Dip-FromLBump` |
| `Copy of 45 Dip from L Bump` | `45-Dip-FromLBump-2` |

### Shoot 8 — `S8-`

| Current | Proposed |
|---|---|
| `Shoot 8 Center` | `S8-Center` |
| `Shoot 8 R trench Forward` | `S8-RTrench-Fwd` |
| `Shoot 8 R trench Backwards` | `S8-RTrench-Back` |
| `Backup shoot 8` | `S8-Backup` |

### Center / Depot / misc — `CTR-`, `DEP-`

| Current | Proposed |
|---|---|
| `Center to Depot` | `CTR-Depot` |
| `Dip 3 to Depot to Mid` | `CTR-Dip3-Depot-Mid` |
| `Depot Backwards + Ocilation` | `DEP-Back-Oscillate` *(also fixes the typo)* |
| `Plow` | `CTR-Plow` |
| `Fast Away` | `CTR-FastAway` |

### Test / dev — `ZZ-` (sorts to the bottom, out of your way)

| Current | Proposed |
|---|---|
| `Test Event Trig` | `ZZ-EventTrig` |
| `Test Event Triggers` | `ZZ-EventTriggers` |
| `Testing` | `ZZ-Testing` |
| `SMR Test` | `ZZ-SMR` |
| `SysCheck` | `ZZ-SysCheck` *(referenced by `SysCheckSequence.java` — rename requires a Java edit)* |

## Applied auto renames (42 autos)

Same prefixes. Autos additionally carry piece count where it's known, since
that's what you actually pick by at competition.

| Current | Proposed |
|---|---|
| `L Trench 2 Dip` | `LT-2Dip` |
| `L Trench 2 Dip + Depot` | `LT-2Dip-Depot` |
| `L Trench Defensive` | `LT-Defensive` |
| `Simple Left` | `LT-Simple` |
| `Simple L NC` | `LT-Simple-NC` |
| `Simple left + Depot` | `LT-Simple-Depot` |
| `L Bump Dip` | `LB-Dip` |
| `Plow L Trench` | `LT-Plow` |
| `Plow L Trench into Outpost` | `LT-Plow-Outpost` |
| `L R Back to Middle NC` | `LT-RT-BackToMid-NC` |
| `R Trench 2 Dips` | `RT-2Dip` |
| `R Trench 2 Dips + Outpost` | `RT-2Dip-Outpost` |
| `R Trench Outpost` | `RT-Outpost` |
| `R Trench to Bump` | `RT-Bump` |
| `Quarter R Trench` | `RT-Quarter` |
| `Plow R Trench` | `RT-Plow` |
| `S R Back to Middle NC` | `RT-Simple-BackToMid-NC` |
| `Simple Right + Outpost` | `RT-Simple-Outpost` |
| `Simple Right + Sweep` | `RT-Simple-Sweep` |
| `Back up Simple Right + Outpost` | `RT-Simple-Outpost-Backup` |
| `Outpost` | `OUT-Base` |
| `Outpost from Bump` | `OUT-FromBump` |
| `Center to Depot` | `CTR-Depot` |
| `2 Dips` | `CTR-2Dip` |
| `Dippy Doo` | `CTR-Dip` *(rename for searchability)* |
| `45 Defensive` | `45-Defensive` |
| `Bumpy Ride L-R` | `BUMP-Ride-LR` |
| `Bumpy Ride R-L` | `BUMP-Ride-RL` |
| `Shoot 8 + Plow` | `S8-Plow` |
| `Shoot 8 R Dip` | `S8-RT-Dip` |
| `Shoot 8 R Trench 2 Dips` | `S8-RT-2Dip` |
| `Fast Away` | `CTR-FastAway` |
| `Slow Lob` | `LT-SlowLob` |
| `SMR 1` | `ZZ-SMR-1` |
| `SMR 5` | `ZZ-SMR-5` |
| `EventTest` | `ZZ-EventTest` |
| `SysCheck` | `ZZ-SysCheck` *(referenced in Java)* |
| `New Auto` | **archive candidate** |
| `New New Auto` | **archive candidate** |
| `New New New Auto` | **archive candidate** |
| `Awesome` | **archive candidate** |
| `Not Awesome` | **archive candidate** |

## Applying this

Renaming an auto is free. Renaming a *path* requires rewriting the `pathName`
field in every `.auto` that references it — doing it by hand in the PathPlanner
GUI silently breaks autos. When you approve a mapping, the rename should be
scripted so paths and references move together, then verified with
`python3 tools/pathplanner_audit.py` (a broken reference shows up as
`references missing path`).
