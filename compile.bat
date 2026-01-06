@echo off
echo ========================================
echo   Bien dich TeamViewer 2.0
echo ========================================
echo.

echo Dang bien dich cac file Java...
if not exist bin mkdir bin
javac -encoding UTF-8 -d bin src\*.java

if errorlevel 1 (
    echo.
    echo ========================================
    echo   Loi bien dich!
    echo ========================================
    echo Vui long kiem tra lai code.
    echo.
    exit /b 1
) else (
    echo.
    echo ========================================
    echo   Bien dich thanh cong!
    echo ========================================
    echo.
    echo Co the chay ung dung bang lenh:
    echo   1. Chay Client/Server: run.bat
    echo   2. Chay All-in-One: run-all.bat
    echo.
)
