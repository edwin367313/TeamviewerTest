@echo off
echo ========================================
echo   Docker - TeamViewer Relay Server
echo ========================================
echo.

echo [1/3] Build Docker image...
docker build -t teamviewer-relay .

if %errorlevel% neq 0 (
    echo Loi build Docker image!
    pause
    exit /b 1
)

echo.
echo [2/3] Stop container cu (neu co)...
docker stop teamviewer-relay 2>nul
docker rm teamviewer-relay 2>nul

echo.
echo [3/3] Khoi dong Relay Server...
echo Server se chay tai port 5900
docker run -d -p 5900:5900 --name teamviewer-relay teamviewer-relay

if %errorlevel% equ 0 (
    echo.
    echo Relay Server da khoi dong thanh cong!
    echo ID Container:
    docker ps -f name=teamviewer-relay --format "{{.ID}}"
) else (
    echo Loi khoi dong container!
)

pause
