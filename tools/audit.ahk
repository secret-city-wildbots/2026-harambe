#Requires AutoHotkey v2.0
#SingleInstance Force
;==============================================================================
; FRC 4265 - PathPlanner hotkeys
;
; Double-click this file to load it. A green "H" appears in your system tray.
;
;   F9          audit the auto you're looking at (text)
;   F10         rebuild + open the visual overlay
;   Ctrl+F9     audit every auto that has an error
;
; These only fire while PathPlanner is the ACTIVE window, so they don't collide
; with VS Code (which uses F9 for breakpoints and F10 for step-over).
;
; Always available, from any window:
;   Ctrl+Alt+F9   audit
;   Ctrl+Alt+F10  visual overlay
;   Ctrl+Alt+I    show the active window's title/exe (for troubleshooting)
;   Ctrl+Alt+R    reload this script after editing it
;
; To load it automatically at login: press Win+R, type  shell:startup , and put
; a shortcut to this file in the folder that opens.
;==============================================================================

REPO := "C:\Users\wildr\Programming\Robotics\FRC-4265\2026-harambe"

SetTitleMatchMode 2            ; "contains" matching on window titles

Audit()     => Run('"' REPO '\tools\audit.bat"',      REPO)
AuditAll()  => Run('"' REPO '\tools\audit-all.bat"',  REPO)
View()      => Run('"' REPO '\tools\view-once.bat"',  REPO)

A_IconTip := "FRC 4265 PathPlanner`nF9 audit | F10 view | Ctrl+F9 audit all"
TrayTip "PathPlanner hotkeys loaded",
        "In PathPlanner:`n  F9  audit this auto`n  F10  visual overlay`n  Ctrl+F9  audit all"

; ---- scoped to PathPlanner ---------------------------------------------------
#HotIf WinActive("PathPlanner")
F9::  Audit()
F10:: View()
^F9:: AuditAll()
#HotIf

; ---- global fallbacks --------------------------------------------------------
^!F9::  Audit()
^!F10:: View()
^!r::   Reload()

^!i:: {
    try
        MsgBox "Active window`n`ntitle: " WinGetTitle("A")
             . "`nexe:   " WinGetProcessName("A")
             . "`n`nIf the title does not contain 'PathPlanner', edit the"
             . " WinActive(...) line in this script to match it."
    catch
        MsgBox "Could not read the active window."
}
