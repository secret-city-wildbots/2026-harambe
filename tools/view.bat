@echo off
REM Visual overlay of every auto: field route + intake/shooter spans.
REM Opens in your browser and rebuilds every time you save in PathPlanner.
REM Just refresh the tab (F5) after a save. Ctrl+C here to stop.
cd /d "%~dp0.."

where py >nul 2>&1 && (set PY=py -3) || (set PY=python)

%PY% tools\pathplanner_visualize.py --watch
