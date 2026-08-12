@echo off
REM Path <-> auto cross-reference. Which autos use this path?
cd /d "%~dp0.."
where py >nul 2>&1 && (set PY=py -3) || (set PY=python)
%PY% tools\pathplanner_map.py --watch
