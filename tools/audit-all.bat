@echo off
REM Text audit of every auto that has an error.
cd /d "%~dp0.."
where py >nul 2>&1 && (set PY=py -3) || (set PY=python)
%PY% tools\pathplanner_audit.py --problems
echo.
pause
