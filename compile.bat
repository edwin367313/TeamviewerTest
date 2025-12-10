@echo off
echo ========================================
echo   Bien dich TeamViewer 2.0
echo ========================================
echo.

echo Dang bien dich cac file Java...
javac -encoding UTF-8 *.java

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo   Bien dich thanh cong!
    echo ========================================
    echo.
    echo Co the chay ung dung bang lenh:
    echo   java TeamViewerGUI
    echo.
) else (
    echo.
    echo ========================================
    echo   Loi bien dich!
    echo ========================================
    echo Vui long kiem tra lai code.
    echo.
)

pause
