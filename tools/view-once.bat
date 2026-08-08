@echo off
REM Rebuild pathplanner_view.html and open it. No watch loop.
cd /d "%~dp0.."
where py >nul 2>&1 && (set PY=py -3) || (set PY=python)
%PY% tools\pathplanner_visualize.py --open
