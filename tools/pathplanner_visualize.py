#!/usr/bin/env python3
"""
Generates pathplanner_view.html -- a visual overlay of what each auto's
mechanisms are doing WHERE on the field.

The point: seeing whether the intake-down span actually covers the stretch
of path you meant it to, instead of counting markers in a list.

    python tools/pathplanner_visualize.py          # build + report
    python tools/pathplanner_visualize.py --open    # build and open it
    python tools/pathplanner_visualize.py --watch   # rebuild on every save

Marker positions are exact: PathPlanner's waypointRelativePos indexes into
the cubic bezier segments, so a marker at 1.81 is 81% along the segment from
waypoint 1 to waypoint 2, and we evaluate that bezier directly.
"""

import argparse
import glob
import json
import os
import sys
import time
import webbrowser

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from pathplanner_audit import (PP, ROOT, TRIGGERS, load, markers_of,  # noqa: E402
                               path_sequence, replay)

# 2026 field extents in metres. Adjust if the official numbers differ.
FIELD_X, FIELD_Y = 17.548, 8.052


def bez(p0, p1, p2, p3, t):
    u = 1 - t
    return (u*u*u*p0[0] + 3*u*u*t*p1[0] + 3*u*t*t*p2[0] + t*t*t*p3[0],
            u*u*u*p0[1] + 3*u*u*t*p1[1] + 3*u*t*t*p2[1] + t*t*t*p3[1])


def pt(wps, rel):
    """Field XY at a waypointRelativePos."""
    n = len(wps) - 1
    if n < 1:
        a = wps[0]["anchor"]
        return (a["x"], a["y"])
    rel = max(0.0, min(rel, n))
    seg = min(int(rel), n - 1)
    t = rel - seg
    a, b = wps[seg], wps[seg + 1]
    p0 = (a["anchor"]["x"], a["anchor"]["y"])
    p3 = (b["anchor"]["x"], b["anchor"]["y"])
    p1 = (a["nextControl"] or a["anchor"])
    p2 = (b["prevControl"] or b["anchor"])
    return bez(p0, (p1["x"], p1["y"]), (p2["x"], p2["y"]), p3, t)


def densify(wps, step=0.02):
    """Polyline of (relpos, x, y) along the whole path."""
    n = max(len(wps) - 1, 1)
    out, r = [], 0.0
    while r < n:
        x, y = pt(wps, r)
        out.append((r, x, y))
        r += step
    x, y = pt(wps, n)
    out.append((n, x, y))
    return out


def build_auto(aname, aj, paths):
    """Return a dict describing one auto: geometry + mechanism spans."""
    seq = []
    path_sequence(aj.get("command") or {}, seq)

    segments = []     # one per path
    events = []       # flattened, in cumulative rel-pos coords
    offset = 0.0
    for kind, val in seq:
        if kind != "path" or val not in paths:
            continue
        wps = paths[val]["waypoints"]
        span = max(len(wps) - 1, 1)
        poly = [(offset + r, x, y) for (r, x, y) in densify(wps)]
        segments.append(dict(name=val, start=offset, end=offset + span, poly=poly,
                             rot=rotation_track(paths[val], offset)))
        for pos, name, _ in markers_of(paths[val]):
            if not name:
                continue
            x, y = pt(wps, pos)
            events.append(dict(g=offset + pos, local=pos, path=val,
                               name=name, x=x, y=y))
        offset += span
    total = offset or 1.0
    events.sort(key=lambda e: e["g"])

    # --- mechanism spans -------------------------------------------------
    intake, shooter, issues = [], [], []
    open_intake = None
    open_shot = None
    shot_n = 0
    for e in events:
        n = e["name"]
        if n == "Intake":
            if open_intake is None:
                open_intake = e["g"]
            else:
                issues.append(dict(g=e["g"], level="warn",
                                   text=f"Intake fired while already down ({e['path']} @{e['local']:.2f})"))
        elif n in ("IntakeRetract", "StopIntake"):
            if open_intake is None:
                issues.append(dict(g=e["g"], level="warn",
                                   text=f"{n} but intake was never down ({e['path']} @{e['local']:.2f})"))
            else:
                intake.append(dict(a=open_intake, b=e["g"]))
                open_intake = None
        elif n == "AimAndShoot":
            if open_shot is None:
                shot_n += 1
                open_shot = (e["g"], shot_n)
            else:
                shooter.append(dict(a=open_shot[0], b=e["g"], n=open_shot[1], open=False))
                open_shot = None
        elif n in ("Shoot", "ShootStop") and open_shot is not None:
            shooter.append(dict(a=open_shot[0], b=e["g"], n=open_shot[1], open=False, killed=True))
            issues.append(dict(g=e["g"], level="err",
                               text=f"'{n}' takes transfer+indexer and CANCELS shot #{open_shot[1]} "
                                    f"-- toggle parity is now inverted for the rest of the auto"))
            open_shot = None

    if open_intake is not None:
        intake.append(dict(a=open_intake, b=total, open=True))
        issues.append(dict(g=total, level="warn", text="auto ends with intake still down"))
    if open_shot is not None:
        shooter.append(dict(a=open_shot[0], b=total, n=open_shot[1], open=True))
        issues.append(dict(g=total, level="warn",
                           text=f"ends with shot #{open_shot[1]} still running "
                                f"(odd AimAndShoot count) -- shooter carries into teleop"))

    _, _, findings = replay(aname, aj, paths)
    for lvl, txt in findings:
        if "NamedCommand" in txt or "missing path" in txt:
            issues.append(dict(g=0, level="err", text=txt))

    return dict(name=aname, total=total, segments=segments, events=events,
                intake=intake, shooter=shooter, issues=issues, shots=shot_n)


def robot_spec():
    """Bumper box + the robotFeatures drawing from settings.json.

    Feature coordinates are robot-relative: +X forward, +Y left, metres.
    rounded_rect 'length' is the X extent and 'width' is the Y extent.
    """
    spec = dict(width=0.85, length=0.85, ox=0.0, oy=0.0, features=[])
    try:
        with open(os.path.join(PP, "settings.json"), encoding="utf-8") as f:
            s = json.load(f)
    except Exception:
        return spec
    spec["width"] = float(s.get("robotWidth", 0.85))
    spec["length"] = float(s.get("robotLength", 0.85))
    spec["ox"] = float(s.get("bumperOffsetX", 0.0))
    spec["oy"] = float(s.get("bumperOffsetY", 0.0))
    for raw in s.get("robotFeatures") or []:
        try:
            f = json.loads(raw) if isinstance(raw, str) else raw
            spec["features"].append(dict(name=f.get("name", ""),
                                         type=f.get("type"),
                                         data=f.get("data") or {}))
        except Exception:
            continue
    return spec


def rotation_track(pj, offset):
    """[(global relpos, degrees)] for a path, start + targets + end."""
    n = max(len(pj.get("waypoints") or []) - 1, 1)
    pts = []
    start = (pj.get("idealStartingState") or {}).get("rotation")
    if start is not None:
        pts.append([offset, float(start)])
    for t in pj.get("rotationTargets") or []:
        p = float(t.get("waypointRelativePos", 0.0))
        pts.append([offset + max(0.0, min(p, n)), float(t.get("rotationDegrees", 0.0))])
    end = (pj.get("goalEndState") or {}).get("rotation")
    if end is not None:
        pts.append([offset + n, float(end)])
    pts.sort(key=lambda x: x[0])
    return pts


def newest_auto():
    files = glob.glob(os.path.join(PP, "autos", "*.auto"))
    if not files:
        return ""
    return os.path.splitext(os.path.basename(max(files, key=os.path.getmtime)))[0]


TEMPLATE = os.path.join(os.path.dirname(os.path.abspath(__file__)), "view_template.html")
# Generated output lives beside this script, not at the repo root, so the
# repo root stays clean. field2026.png is in here too, which is why the
# HTML can reference it with a bare relative path.
OUT_DIR = os.path.dirname(os.path.abspath(__file__))


def build(open_after=False, quiet=False):
    autos, paths = load("autos/*.auto"), load("paths/*.path")
    data = {n: build_auto(n, aj, paths) for n, aj in autos.items()}
    robot = robot_spec()
    default = newest_auto()

    with open(TEMPLATE, encoding="utf-8") as f:
        html = f.read()
    for token, value in (("__DATA__", json.dumps(data)),
                         ("__FX__", repr(FIELD_X)), ("__FY__", repr(FIELD_Y)),
                         ("__ROBOT__", json.dumps(robot)),
                         ("__DEFAULT__", json.dumps(default))):
        html = html.replace(token, value)

    out = os.path.join(OUT_DIR, "pathplanner_view.html")
    with open(out, "w", encoding="utf-8") as f:
        f.write(html)
    if not quiet:
        bad = [n for n, d in data.items() if any(i["level"] == "err" for i in d["issues"])]
        print(f"wrote {out}")
        print(f"{len(data)} autos, {len(bad)} with errors")
        print(f"opens on: {default}  (most recently saved)")
    if open_after:
        webbrowser.open("file:///" + out.replace("\\", "/"))
    return out


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--open", action="store_true", help="open in your browser")
    ap.add_argument("--watch", action="store_true", help="rebuild on every save")
    args = ap.parse_args()

    if not args.watch:
        build(open_after=args.open)
        return 0

    build(open_after=True, quiet=True)
    print("Watching. Save in PathPlanner, then refresh the browser tab. Ctrl+C to stop.")
    last = None
    try:
        while True:
            st = tuple(sorted((f, os.path.getmtime(f))
                              for f in glob.glob(os.path.join(PP, "**", "*.*"), recursive=True)))
            if st != last:
                last = st
                build(quiet=True)
                print(f"  rebuilt {time.strftime('%H:%M:%S')}")
            time.sleep(1.0)
    except KeyboardInterrupt:
        print("\nstopped.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
