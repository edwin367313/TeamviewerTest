@echo off
echo ========================================
echo   Chay TeamViewer 2.0
echo ========================================
echo.

if not exist TeamViewerGUI.class (
    echo Chua bien dich! Dang bien dich...
    call compile.bat
    echo.
)

echo Dang khoi dong TeamViewer 2.0...
java TeamViewerGUI

pause
