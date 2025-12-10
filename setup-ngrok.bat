@echo off
echo ========================================
echo   Huong dan su dung Ngrok
echo ========================================
echo.
echo BUOC 1: Dang ky Ngrok (MIEN PHI)
echo   - Truy cap: https://dashboard.ngrok.com/signup
echo   - Dang ky bang Google/Github hoac email
echo.
echo BUOC 2: Lay Authtoken
echo   - Sau khi dang nhap: https://dashboard.ngrok.com/get-started/your-authtoken
echo   - Copy authtoken (VD: 2abc123def456...)
echo.
echo BUOC 3: Cau hinh Ngrok
echo   - Chay lenh:
echo     ngrok\ngrok.exe config add-authtoken YOUR_TOKEN
echo.
echo BUOC 4: Chay tunnel
echo   - ngrok\ngrok.exe tcp 5900
echo.
echo ========================================
echo.

REM Mo trang dang ky
start https://dashboard.ngrok.com/signup

echo Da mo trang dang ky Ngrok trong trinh duyet.
echo.
echo Sau khi lay duoc authtoken, nhap vao day:
set /p TOKEN="Authtoken cua ban: "

if "%TOKEN%"=="" (
    echo.
    echo Chua nhap token! Vui long chay lai va nhap token.
    pause
    exit /b 1
)

echo.
echo Dang cau hinh authtoken...
ngrok\ngrok.exe config add-authtoken %TOKEN%

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo   Cau hinh thanh cong!
    echo ========================================
    echo.
    echo Khoi dong tunnel...
    echo.
    ngrok\ngrok.exe tcp 5900
) else (
    echo.
    echo Loi cau hinh! Vui long kiem tra lai token.
)

pause
