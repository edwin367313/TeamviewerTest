# TeamViewer 2.0 Run All
Write-Host "Khoi dong TeamViewer 2.0..."

$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptPath

Write-Host "Bien dich code..."
& "$scriptPath\compile.bat"

Write-Host "Mo VS Code..."
Start-Process code -ArgumentList $scriptPath

Start-Sleep 2

Write-Host "Khoi dong Server..."
Start-Process powershell -ArgumentList "-NoExit","-Command","cd '$scriptPath'; java -cp bin Server"

Start-Sleep 3

Write-Host "Khoi dong Client..."
Start-Process powershell -ArgumentList "-NoExit","-Command","cd '$scriptPath'; java -cp bin TeamViewerGUI"

Write-Host "Hoan thanh!"
Read-Host "Enter de dong"
