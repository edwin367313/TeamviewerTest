@echo off
echo ========================================
echo   Docker - TeamViewer 2.0 Server
echo ========================================
echo.

echo [1/3] Build Docker image...
docker build -t teamviewer-server .

if %errorlevel% neq 0 (
    echo Loi build Docker image!
    pause
    exit /b 1
)

echo.
echo [2/3] Stop container cu (neu co)...
docker stop teamviewer-server 2>nul
docker rm teamviewer-server 2>nul

echo.
echo [3/3] Chay container moi...
docker run -d ^
    --name teamviewer-server ^
    -p 5900:5900 ^
    teamviewer-server

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo   Server dang chay!
    echo ========================================
    echo.
    echo Port: 5900
    echo.
    
    echo Lay IP public:
    curl -s https://api.ipify.org
    echo.
    echo.
    echo Client co the ket noi den: [IP_PUBLIC]:5900
    echo.
    echo Xem logs:  docker logs -f teamviewer-server
    echo Dung:      docker stop teamviewer-server
    echo.
) else (
    echo Loi khi chay container!
)

pause
