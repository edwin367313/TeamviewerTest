@echo off
echo ========================================
echo   Ngrok Tunnel - TeamViewer 2.0
echo ========================================
echo.

echo Tao tunnel cho port 5900...
echo.
echo Luu y: Ban can cai dat ngrok truoc!
echo Download: https://ngrok.com/download
echo.

pause

echo Dang khoi dong ngrok...
echo.
ngrok tcp 5900

REM Sau khi ngrok chay, ban se thay dia chi nhu:
REM tcp://0.tcp.ngrok.io:12345
REM Client se ket noi: 0.tcp.ngrok.io:12345
