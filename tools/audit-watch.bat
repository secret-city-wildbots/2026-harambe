@echo off
REM Live audit. Leave this window open next to PathPlanner; every time you
REM save, it re-runs itself. No button press needed at all.
REM Ctrl+C to stop.
cd /d "%~dp0.."

where py >nul 2>&1 && (set PY=py -3) || (set PY=python)

%PY% tools\pathplanner_audit.py --watch --last
