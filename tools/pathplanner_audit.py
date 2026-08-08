#!/usr/bin/env python3
"""
PathPlanner event-trigger auditor for FRC 4265.

Expands every .auto into its full ordered path sequence, replays the
EventTrigger markers in firing order, and tells you what the robot will
actually do -- so you don't have to count AimAndShoot markers by hand.

AimAndShoot is bound with .toggleOnTrue, so it alternates START / STOP.
That's intentional. What this tool does is COUNT THE PARITY FOR YOU and
flag the cases where the count silently desyncs from what you expect.

Usage
-----
    python tools/pathplanner_audit.py                 # audit everything
    python tools/pathplanner_audit.py --last          # audit the most recently
                                                      #   saved auto (the one you
                                                      #   are looking at)
    python tools/pathplanner_audit.py --auto "LT-2Dip"
    python tools/pathplanner_audit.py --watch         # re-audit on every save
    python tools/pathplanner_audit.py --problems      # only autos with problems
    python tools/pathplanner_audit.py --orphans       # unreferenced paths

The TRIGGERS table below mirrors the EventTrigger bindings in
RobotContainer.java. If you add or change a binding there, change it here too.
"""

import argparse
import glob
import json
import os
import sys
import time
from collections import Counter, defaultdict

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
PP = os.path.join(ROOT, "src", "main", "deploy", "pathplanner")

# ---------------------------------------------------------------------------
# VERIFY THIS BEFORE TRUSTING THE ANALYSIS IN THIS REPO. Carried over from
# 2026-Robot: as of the port this repo binds no EventTriggers and none of the
# commands named below exist here, so it is a template. A wrong entry produces
# confidently wrong findings.
#
# Model of the EventTrigger bindings in RobotContainer.java.
#   kind:     "onTrue" | "toggleOnTrue"
#   forever:  True if the command has no isFinished(), so it holds its
#             requirements until something else cancels it.
# ---------------------------------------------------------------------------
TRIGGERS = {
    "Intake":        dict(kind="onTrue", cmd="AutoIntakeExtend",
                          reqs={"intake", "intakeExtension"}, forever=False),
    "IntakeRetract": dict(kind="onTrue", cmd="AutoIntakeRetract",
                          reqs={"intake", "intakeExtension"}, forever=False),
    "StopIntake":    dict(kind="onTrue", cmd="AutoIntakeStop",
                          reqs={"intake"}, forever=False),
    "Shoot":         dict(kind="onTrue", cmd="AutoStartIndexCommand",
                          reqs={"transfer", "indexer"}, forever=False),
    "ShootStop":     dict(kind="onTrue", cmd="AutoStopIndexCommand",
                          reqs={"transfer", "indexer"}, forever=False),
    "AimAndShoot":   dict(kind="toggleOnTrue", cmd="AimAndShootCommand",
                          reqs={"shooter", "transfer", "indexer"}, forever=True),
}

ERR, WARN, INFO = "ERROR", "WARN", "INFO"


def load(pattern):
    out = {}
    for p in sorted(glob.glob(os.path.join(PP, pattern))):
        with open(p, encoding="utf-8") as f:
            out[os.path.splitext(os.path.basename(p))[0]] = json.load(f)
    return out


def path_sequence(node, seq):
    """Depth-first walk of an auto's command tree, collecting paths in order."""
    if not isinstance(node, dict):
        return
    kind, data = node.get("type"), (node.get("data") or {})
    if kind == "path" and data.get("pathName"):
        seq.append(("path", data["pathName"]))
    if kind == "named" and data.get("name"):
        seq.append(("named", data["name"]))
    for child in data.get("commands") or []:
        path_sequence(child, seq)
    if data.get("command"):
        path_sequence(data["command"], seq)


def markers_of(pj):
    ms = []
    for m in pj.get("eventMarkers") or []:
        ms.append((float(m.get("waypointRelativePos") or 0.0),
                   m.get("name") or "",
                   bool(m.get("command"))))
    return sorted(ms, key=lambda x: x[0])


# ---------------------------------------------------------------------------
# Static per-path checks
# ---------------------------------------------------------------------------
def static_issues(paths):
    issues = defaultdict(list)
    for pname, pj in paths.items():
        nwp = len(pj.get("waypoints") or [])
        max_pos = max(nwp - 1, 0)
        at = Counter()
        for pos, name, embedded in markers_of(pj):
            if not name:
                issues["unnamed marker"].append(f"{pname}: @ {pos:.2f}")
                continue
            if name not in TRIGGERS:
                issues["trigger not bound in RobotContainer"].append(
                    f"{pname}: '{name}' @ {pos:.2f}")
            if pos > max_pos + 1e-6:
                issues["marker past end of path (NEVER FIRES)"].append(
                    f"{pname}: '{name}' @ {pos:.2f}, path has {nwp} waypoints")
            if embedded:
                issues["embedded command instead of bare trigger"].append(
                    f"{pname}: '{name}' @ {pos:.2f}")
            at[round(pos, 3)] += 1
        for pos, n in at.items():
            if n > 1:
                issues["markers at identical position (undefined firing order)"].append(
                    f"{pname}: {n} markers @ {pos:.2f}")
    return issues


# ---------------------------------------------------------------------------
# Per-auto replay
# ---------------------------------------------------------------------------
def replay(aname, aj, paths):
    """Return (timeline_lines, findings) for one auto."""
    seq = []
    path_sequence(aj.get("command") or {}, seq)

    findings = []          # (level, text)
    events = []            # (path, pos, trigger)
    npaths = 0

    for kind, val in seq:
        if kind == "named":
            findings.append((ERR,
                f"auto-level NamedCommand '{val}': NamedCommands.registerCommand "
                f"is never called anywhere in the Java, so this auto fails to load"))
            continue
        npaths += 1
        if val not in paths:
            findings.append((ERR, f"references missing path '{val}'"))
            continue
        for pos, name, _ in markers_of(paths[val]):
            if name:
                events.append((val, pos, name))

    # --- state ---
    shot_running = False       # is AimAndShootCommand scheduled?
    shot_started_at = None
    held = {}                  # requirement -> (cmd, path, pos)
    intake = "retracted"
    lines = []
    shot_n = 0

    for pth, pos, name in events:
        b = TRIGGERS.get(name)
        loc = f"{pth} @{pos:.2f}"
        if b is None:
            findings.append((ERR, f"[{loc}] '{name}' is not bound in RobotContainer"))
            continue

        # ---------- AimAndShoot: the toggle ----------
        if name == "AimAndShoot":
            if shot_running:
                shot_running = False
                held = {k: v for k, v in held.items() if v[0] != "AimAndShootCommand"}
                lines.append(f"    [{loc}] AimAndShoot  ->  STOP shot #{shot_n}")
            else:
                shot_n += 1
                shot_running = True
                shot_started_at = loc
                for r in b["reqs"]:
                    held[r] = ("AimAndShootCommand", pth, pos)
                lines.append(f"    [{loc}] AimAndShoot  ->  START shot #{shot_n}")
            continue

        # ---------- everything else ----------
        stolen = {}
        for r in b["reqs"]:
            if r in held and held[r][0] != b["cmd"]:
                stolen.setdefault(held[r], set()).add(r)

        for (ocmd, opth, opos), rs in stolen.items():
            if ocmd == "AimAndShootCommand":
                findings.append((ERR,
                    f"[{loc}] '{name}' requires {sorted(rs)} and CANCELS the shot "
                    f"started at [{opth} @{opos:.2f}]. "
                    f"This breaks your even/odd count: the toggle silently flips back "
                    f"to OFF without an AimAndShoot marker, so your NEXT AimAndShoot "
                    f"marker will START a shot when you expect it to stop one."))
                shot_running = False
                lines.append(f"    [{loc}] {name}  ->  cancels shot #{shot_n} (DESYNC)")
            held = {k: v for k, v in held.items() if v[0] != ocmd}

        if not stolen:
            lines.append(f"    [{loc}] {name}")

        # intake state machine
        if name == "Intake":
            if intake == "extended":
                findings.append((WARN, f"[{loc}] 'Intake' fired while intake is already extended"))
            intake = "extended"
        elif name in ("IntakeRetract", "StopIntake"):
            if intake != "extended":
                findings.append((WARN, f"[{loc}] '{name}' fired but intake was never extended"))
            intake = "retracted"

    # --- end-of-auto state ---
    if shot_running:
        # Intentional: leaving the shooter spun up into teleop is a valid choice.
        # Reported so the parity is visible, not because it is wrong.
        findings.append((WARN,
            f"ends with shot #{shot_n} still running (started at [{shot_started_at}]) "
            f"— odd AimAndShoot count, shooter carries into teleop"))
    if intake == "extended":
        findings.append((WARN, "auto ends with intake still extended"))

    summary = (f"{npaths} paths, {len(events)} markers, "
               f"{shot_n} shot(s), shooter {'RUNNING' if shot_running else 'off'} at end")
    return summary, lines, findings


# ---------------------------------------------------------------------------
def orphans(autos, paths):
    used = set()
    for aj in autos.values():
        seq = []
        path_sequence(aj.get("command") or {}, seq)
        used |= {v for k, v in seq if k == "path"}
    return sorted(set(paths) - used)


def most_recent_auto():
    files = glob.glob(os.path.join(PP, "autos", "*.auto"))
    if not files:
        return None
    newest = max(files, key=os.path.getmtime)
    return os.path.splitext(os.path.basename(newest))[0]


def run(args):
    autos, paths = load("autos/*.auto"), load("paths/*.path")

    if args.orphans:
        for o in orphans(autos, paths):
            print(o)
        return 0

    target = args.auto
    if args.last:
        target = most_recent_auto()

    print("=" * 74)
    if target:
        print(f"PATHPLANNER AUDIT  -  {target}")
    else:
        print(f"PATHPLANNER AUDIT  -  {len(paths)} paths, {len(autos)} autos")
    print("=" * 74)

    if not target:
        orph = orphans(autos, paths)
        if orph:
            print(f"\nORPHANED PATHS ({len(orph)}) - no auto references these:")
            for o in orph:
                print(f"  {o}")
        iss = static_issues(paths)
        if iss:
            print("\nSTATIC ISSUES")
            for cat, items in sorted(iss.items()):
                print(f"\n  [{cat}]  ({len(items)})")
                for it in items:
                    print(f"    {it}")
        print("\n" + "=" * 74)
        print("PER-AUTO REPLAY")
        print("=" * 74)

    if target and target not in autos:
        print(f"\n  no auto named '{target}'")
        return 2

    names = [target] if target else sorted(autos)
    bad = 0
    for aname in names:
        summary, lines, findings = replay(aname, autos[aname], paths)
        errs = [f for f in findings if f[0] == ERR]
        if errs:
            bad += 1
        if args.problems and not errs:
            continue
        print(f"\n{aname}")
        print(f"  {summary}")
        if lines and (target or args.timeline):
            print("  timeline:")
            for ln in lines:
                print(ln)
        if not findings:
            print("  OK")
        for lvl, txt in findings:
            print(f"  {'X' if lvl == ERR else '~'} {txt}")

    if not target:
        print(f"\n{bad} of {len(names)} autos have errors.")
    return 1 if bad else 0


def watch(args):
    print("Watching for PathPlanner saves. Ctrl+C to stop.\n")
    last = None
    try:
        while True:
            stamp = tuple(sorted(
                (f, os.path.getmtime(f))
                for f in glob.glob(os.path.join(PP, "**", "*.*"), recursive=True)))
            if stamp != last:
                last = stamp
                os.system("cls" if os.name == "nt" else "clear")
                run(args)
                print("\n[watching... save in PathPlanner to re-run]")
            time.sleep(1.0)
    except KeyboardInterrupt:
        print("\nstopped.")
    return 0


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--auto", help="audit one auto by name")
    ap.add_argument("--last", action="store_true",
                    help="audit the most recently saved auto")
    ap.add_argument("--watch", action="store_true",
                    help="re-run automatically whenever a file is saved")
    ap.add_argument("--problems", action="store_true",
                    help="only show autos that have errors")
    ap.add_argument("--timeline", action="store_true",
                    help="show the marker timeline for every auto")
    ap.add_argument("--orphans", action="store_true",
                    help="list paths no auto references")
    args = ap.parse_args()
    return watch(args) if args.watch else run(args)


if __name__ == "__main__":
    sys.exit(main())
