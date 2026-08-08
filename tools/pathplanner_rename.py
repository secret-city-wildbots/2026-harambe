#!/usr/bin/env python3
"""
One-shot rename of PathPlanner paths and autos to the prefix scheme in
PATHPLANNER_NAMING_PROPOSAL.md, rewriting every pathName reference so no
auto breaks.

    python tools/pathplanner_rename.py --dry-run
    python tools/pathplanner_rename.py --apply

Safe to re-run: names already in their target form are skipped.
"""

import argparse
import glob
import json
import os
import sys

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
PP = os.path.join(ROOT, "src", "main", "deploy", "pathplanner")

PATHS = {
    # Left Trench
    "L Trench": "LT-Base",
    "L Trench Face Forwards": "LT-Base-Fwd",
    "L Trench Reverse": "LT-Base-Rev",
    "L Trench over Bump": "LT-Base-OB",
    "L Trench 45": "LT-45",
    "L Trench Back to Mid": "LT-BackToMid",
    "L Trench Dip 1": "LT-Dip1",
    "L Trench Dip 2": "LT-Dip2",
    "L Trench Dip 3 and Push": "LT-Dip3Push",
    "L Trench RDip 1": "LT-RDip1",
    "L Trench RDip 2": "LT-RDip2",
    "L Trench + Depot + Pickup": "LT-Depot-Pickup",
    "L Trench to Depot to Mid": "LT-Depot-Mid",
    "L Trench + Plow": "LT-Plow",
    "L Trench from R Plow": "LT-FromRPlow",
    "L Trench Slow Lob": "LT-SlowLob",
    # Right Trench
    "R Trench": "RT-Base",
    "R Trench Reverse": "RT-Base-Rev",
    "R Trench Reverse (over bump)": "RT-Base-Rev-OB",
    "Copy of R Trench Reverse (over bump)": "RT-Base-Rev-OB-2",
    "Front end R Trench Reverse": "RT-Base-Rev-Fwd",
    "R Trench Back to Mid": "RT-BackToMid",
    "R Trench Dip": "RT-Dip1",
    "Copy of R Trench Dip": "RT-Dip2",
    "R Trench Quarter": "RT-Quarter",
    "R Trench Quarter Reverse": "RT-Quarter-Rev",
    "R Trench + Shoot": "RT-Shoot",
    "R Trench Shoot": "RT-Shoot-2",
    "R Trench + Plow": "RT-Plow",
    "R Trench from L Plow": "RT-FromLPlow",
    # Left Bump
    "L Bump Dip": "LB-Dip",
    "L Bump 45 to Depot": "LB-45-Depot",
    "L Bump to Depot to Climb": "LB-Depot-Climb",
    "L Bump Plow P.1": "LB-Plow-P1",
    "L Bump Plow P.2": "LB-Plow-P2",
    # Right Bump
    "R Bump to R Trench": "RB-RTrench",
    "R Bump to Outpost": "RB-Outpost",
    "R Bump into Outpost": "RB-Outpost-In",
    "R Bump to Outpost sideways": "RB-Outpost-Side",
    "R Bump Plow P.1": "RB-Plow-P1",
    "R Bump Plow P.2": "RB-Plow-P2",
    # Outpost
    "Outpost + Shoot": "OUT-Shoot",
    "Outpost Sweep": "OUT-Sweep",
    "Outpost to Mid": "OUT-Mid",
    "Outpost to Mid Dip": "OUT-MidDip",
    "Outpost to hub": "OUT-Hub",
    "Outpost to R Trench + Shoot": "OUT-RTrench-Shoot",
    "Sideways outpost to R Trench": "OUT-Side-RTrench",
    # 45 start
    "45 to L Bump": "45-LBump",
    "Copy of 45 to L Bump": "45-LBump-2",
    "45 to Mid": "45-Mid",
    "45 Dip from L Bump": "45-Dip-FromLBump",
    "Copy of 45 Dip from L Bump": "45-Dip-FromLBump-2",
    # Shoot 8
    "Shoot 8 Center": "S8-Center",
    "Shoot 8 R trench Forward": "S8-RTrench-Fwd",
    "Shoot 8 R trench Backwards": "S8-RTrench-Back",
    "Backup shoot 8": "S8-Backup",
    # Center / Depot / misc
    "Center to Depot": "CTR-Depot",
    "Dip 3 to Depot to Mid": "CTR-Dip3-Depot-Mid",
    "Depot Backwards + Ocilation": "DEP-Back-Oscillate",
    "Plow": "CTR-Plow",
    "Fast Away": "CTR-FastAway",
    # Test / dev
    "Test Event Trig": "ZZ-EventTrig",
    "Test Event Triggers": "ZZ-EventTriggers",
    "Testing": "ZZ-Testing",
    "SMR Test": "ZZ-SMR",
    "SysCheck": "ZZ-SysCheck",
}

AUTOS = {
    # Left Trench
    "L Trench 2 Dip": "LT-2Dip",
    "L Trench 2 Dip + Depot": "LT-2Dip-Depot",
    "L Trench Defensive": "LT-Defensive",
    "Simple Left": "LT-Simple",
    "Simple L NC": "LT-Simple-NC",
    "Simple left + Depot": "LT-Simple-Depot",
    "Plow L Trench": "LT-Plow",
    "Plow L Trench into Outpost": "LT-Plow-Outpost",
    "Slow Lob": "LT-SlowLob",
    "L R Back to Middle NC": "LT-RT-BackToMid-NC",
    # Left Bump
    "L Bump Dip": "LB-Dip",
    # Right Trench
    "R Trench 2 Dips": "RT-2Dip",
    "R Trench 2 Dips + Outpost": "RT-2Dip-Outpost",
    "R Trench Outpost": "RT-Outpost",
    "R Trench to Bump": "RT-Bump",
    "Quarter R Trench": "RT-Quarter",
    "Plow R Trench": "RT-Plow",
    "S R Back to Middle NC": "RT-Simple-BackToMid-NC",
    "Simple Right + Outpost": "RT-Simple-Outpost",
    "Simple Right + Sweep": "RT-Simple-Sweep",
    "Back up Simple Right + Outpost": "RT-Simple-Outpost-Backup",
    # Outpost
    "Outpost": "OUT-Base",
    "Outpost from Bump": "OUT-FromBump",
    # Center
    "Center to Depot": "CTR-Depot",
    "2 Dips": "CTR-2Dip",
    "Dippy Doo": "CTR-Dip",
    "Fast Away": "CTR-FastAway",
    # 45
    "45 Defensive": "45-Defensive",
    # Bump traverse
    "Bumpy Ride L-R": "BUMP-Ride-LR",
    "Bumpy Ride R-L": "BUMP-Ride-RL",
    # Shoot 8
    "Shoot 8 + Plow": "S8-Plow",
    "Shoot 8 R Dip": "S8-RT-Dip",
    "Shoot 8 R Trench 2 Dips": "S8-RT-2Dip",
    # Test / dev / stale
    "SMR 1": "ZZ-SMR-1",
    "SMR 5": "ZZ-SMR-5",
    "EventTest": "ZZ-EventTest",
    "SysCheck": "ZZ-SysCheck",
    "New Auto": "ZZ-Old-NewAuto",
    "New New Auto": "ZZ-Old-NewNewAuto",
    "New New New Auto": "ZZ-Old-NewNewNewAuto",
    "Awesome": "ZZ-Old-Awesome",
    "Not Awesome": "ZZ-Old-NotAwesome",
}


def existing(kind):
    ext = ".auto" if kind == "autos" else ".path"
    return {os.path.splitext(os.path.basename(p))[0]
            for p in glob.glob(os.path.join(PP, kind, "*" + ext))}


def check(mapping, kind, problems, notes):
    """problems block the run; notes are informational only."""
    have = existing(kind)
    targets = {}
    for old, new in mapping.items():
        if old not in have:
            if new not in have:
                # Benign: this repo simply does not have that file. Only an
                # unmapped file still sitting on disk is dangerous.
                notes.append(f"{kind}: '{old}' not present in this repo, skipping")
            continue
        if new in targets:
            problems.append(f"{kind}: '{old}' and '{targets[new]}' both map to '{new}'")
        targets[new] = old
        if new in have and new != old:
            problems.append(f"{kind}: target '{new}' already exists on disk")
    for name in sorted(have - set(mapping) - set(mapping.values())):
        problems.append(f"{kind}: '{name}' has no mapping -- it would keep its old name")
    return problems


def rewrite_refs(node, changed):
    if not isinstance(node, dict):
        return
    data = node.get("data") or {}
    if node.get("type") == "path":
        pn = data.get("pathName")
        if pn in PATHS:
            data["pathName"] = PATHS[pn]
            changed.append((pn, PATHS[pn]))
    for c in data.get("commands") or []:
        rewrite_refs(c, changed)
    if data.get("command"):
        rewrite_refs(data["command"], changed)


def main():
    ap = argparse.ArgumentParser()
    g = ap.add_mutually_exclusive_group(required=True)
    g.add_argument("--dry-run", action="store_true")
    g.add_argument("--apply", action="store_true")
    args = ap.parse_args()

    problems, notes = [], []
    check(PATHS, "paths", problems, notes)
    check(AUTOS, "autos", problems, notes)
    if notes:
        print(f"notes ({len(notes)}):")
        for n in notes:
            print("  " + n)
    if problems:
        print("PRE-FLIGHT PROBLEMS:")
        for p in problems:
            print("  " + p)
        if args.apply:
            print("\nrefusing to apply. fix the mapping first.")
            return 2
    else:
        print("pre-flight clean.")

    # 1. rewrite pathName references inside every auto
    nrefs = 0
    for f in sorted(glob.glob(os.path.join(PP, "autos", "*.auto"))):
        with open(f, encoding="utf-8") as fh:
            j = json.load(fh)
        changed = []
        rewrite_refs(j.get("command") or {}, changed)
        if changed:
            nrefs += len(changed)
            print(f"  {os.path.basename(f)}: " +
                  ", ".join(f"{a} -> {b}" for a, b in changed))
            if args.apply:
                with open(f, "w", encoding="utf-8") as fh:
                    json.dump(j, fh, indent=2)
                    fh.write("\n")

    # 2. rename the files
    nfiles = 0
    for kind, mapping, ext in (("paths", PATHS, ".path"), ("autos", AUTOS, ".auto")):
        for old, new in sorted(mapping.items()):
            src = os.path.join(PP, kind, old + ext)
            dst = os.path.join(PP, kind, new + ext)
            if not os.path.exists(src) or src == dst:
                continue
            nfiles += 1
            print(f"  rename {kind}/{old}{ext} -> {new}{ext}")
            if args.apply:
                os.rename(src, dst)

    print(f"\n{nrefs} references rewritten, {nfiles} files renamed"
          f"{'' if args.apply else '  (DRY RUN - nothing written)'}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
