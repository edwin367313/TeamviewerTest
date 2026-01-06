# ========================================
#   TeamViewer 2.0 - Run All Script (PowerShell)
# ========================================

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  TeamViewer 2.0 - Khoi dong Full" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Lấy đường dẫn thư mục hiện tại
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptPath

# [1/3] Compile code
Write-Host "[1/3] Bien dich code..." -ForegroundColor Yellow
& "$scriptPath\compile.bat"
if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "Bien dich that bai! Dung lai." -ForegroundColor Red
    Read-Host "Nhan Enter de thoat"
    exit 1
}

Write-Host ""
Write-Host "[2/3] Mo VS Code workspace..." -ForegroundColor Yellow
Start-Process "code" -ArgumentList $scriptPath -WindowStyle Normal

Start-Sleep -Seconds 2

Write-Host ""
Write-Host "[3/3] Khoi dong Server va Client..." -ForegroundColor Yellow
Write-Host ""

# Chạy Server trong terminal riêng
$serverTitle = "TeamViewer Server"
Start-Process powershell -ArgumentList @(
    "-NoExit",
    "-Command",
    "Write-Host '========== TeamViewer Server ==========' -ForegroundColor Green; Set-Location '$scriptPath'; java -cp bin Server"
) -WindowStyle Normal

Write-Host "- Server dang khoi dong..." -ForegroundColor Green

# Đợi 3 giây để Server khởi động
Start-Sleep -Seconds 3

# Chạy Client trong terminal riêng
$clientTitle = "TeamViewer Client"
Start-Process powershell -ArgumentList @(
    "-NoExit",
    "-Command",
    "Write-Host '========== TeamViewer Client ==========' -ForegroundColor Cyan; Set-Location '$scriptPath'; java -cp bin TeamViewerGUI"
) -WindowStyle Normal

Write-Host "- Client dang khoi dong..." -ForegroundColor Cyan

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  Khoi dong thanh cong!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "- VS Code: Da mo workspace" -ForegroundColor White
Write-Host "- Server: Dang chay trong terminal rieng" -ForegroundColor White
Write-Host "- Client: Dang chay trong terminal rieng" -ForegroundColor White
Write-Host ""
Write-Host "Dong cac terminal de ket thuc ung dung." -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Read-Host "Nhan Enter de dong script nay"
