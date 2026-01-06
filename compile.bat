@echo off
echo ========================================
echo   Bien dich TeamViewer 2.0
echo ========================================
echo.

echo Dang bien dich cac file Java...
if not exist bin mkdir bin
javac -encoding UTF-8 -d bin -sourcepath src src\*.java

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo   Bien dich thanh cong!
    echo ========================================
    echo.
    echo Co the chay ung dung bang lenh:
    echo   1. Chay Client/Server: run.bat
    echo   2. Chay Relay Server (Docker): docker-run.bat
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
