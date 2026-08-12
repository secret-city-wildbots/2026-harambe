;==============================================================================
; FRC 4265 - PathPlanner hotkeys  (AutoHotkey v1 ONLY)
;
; Use this file only if audit.ahk errors on load -- that means you installed
; AutoHotkey v1 instead of v2. Otherwise ignore this file and use audit.ahk.
;
;   F9 / F10 / Ctrl+F9  while PathPlanner is focused
;   Ctrl+Alt+F9 / Ctrl+Alt+F10  from anywhere
;   Ctrl+Alt+I  show active window info    Ctrl+Alt+R  reload
;==============================================================================

#SingleInstance Force
SetTitleMatchMode, 2

REPO := "C:\Users\wildr\Programming\Robotics\FRC-4265\2026-harambe"

Menu, Tray, Tip, FRC 4265 PathPlanner`nF9 audit | F10 view | Ctrl+F9 audit all
TrayTip, PathPlanner hotkeys loaded, In PathPlanner:`n  F9 audit`n  F10 overlay`n  Ctrl+F9 audit all

#If WinActive("PathPlanner")
F9::   Run, "%REPO%\tools\audit.bat", %REPO%
       return
F10::  Run, "%REPO%\tools\view-once.bat", %REPO%
       return
^F9::  Run, "%REPO%\tools\audit-all.bat", %REPO%
       return
#If

^!F9::  Run, "%REPO%\tools\audit.bat", %REPO%
        return
^!F10:: Run, "%REPO%\tools\view-once.bat", %REPO%
        return
^!r::   Reload
        return

^!i::
    WinGetTitle, t, A
    WinGet, e, ProcessName, A
    MsgBox, Active window`n`ntitle: %t%`nexe:   %e%`n`nIf the title does not contain "PathPlanner", edit the WinActive(...) line.
    return
