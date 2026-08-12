@echo off
REM Audit the auto you are currently looking at in PathPlanner.
REM PathPlanner saves on edit, so "most recently saved auto" is the one
REM you have open. Pin this file to your taskbar for one-click checking.
cd /d "%~dp0.."

where py >nul 2>&1 && (set PY=py -3) || (set PY=python)

if "%~1"=="" (
    %PY% tools\pathplanner_audit.py --last
) else (
    %PY% tools\pathplanner_audit.py --auto "%~1"
)

echo.
pause
