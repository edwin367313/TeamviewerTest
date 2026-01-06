@echo off
REM ========================================
REM   TeamViewer 2.0 - Run All Script
REM ========================================
echo.
echo ========================================
echo   TeamViewer 2.0 - Khoi dong Full
echo ========================================
echo.

REM Bien dich truoc
echo [1/3] Bien dich code...
call compile.bat
if %errorlevel% neq 0 (
    echo.
    echo Bien dich that bai! Dung lai.
    pause
    exit /b 1
)

echo.
echo [2/3] Mo VS Code workspace...
start "" code .

timeout /t 2 /nobreak >nul

echo.
echo [3/3] Khoi dong Server va Client...
echo.

REM Chay Server trong terminal rieng
start "TeamViewer Server" cmd /k "echo ========== TeamViewer Server ========== && cd /d %~dp0 && java -cp bin Server"

REM Doi 3 giay de Server khoi dong
timeout /t 3 /nobreak >nul

REM Chay Client trong terminal rieng
start "TeamViewer Client" cmd /k "echo ========== TeamViewer Client ========== && cd /d %~dp0 && java -cp bin TeamViewerGUI"

echo.
echo ========================================
echo   Khoi dong thanh cong!
echo ========================================
echo.
echo - VS Code: Da mo workspace
echo - Server: Dang chay trong terminal rieng
echo - Client: Dang chay trong terminal rieng
echo.
echo Dong terminal nay de ket thuc tat ca.
echo ========================================
pause
