# 停车场管理系统 - 一键启动脚本 (PowerShell)
$ErrorActionPreference = "Stop"

$ProjectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ProjectDir

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  停车场管理系统 - 正在启动前后端服务" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "后端地址: http://localhost:8080" -ForegroundColor Green
Write-Host "前端地址: http://localhost:3000" -ForegroundColor Green
Write-Host ""
Write-Host "[提示] 请确保 MySQL 服务已启动" -ForegroundColor Yellow
Write-Host ""

# 检查必要工具
try {
    $mvnVersion = mvn --version 2>&1
    Write-Host "[√] Maven 已就绪" -ForegroundColor Green
} catch {
    Write-Host "[×] 未找到 Maven，请确认已安装 Maven 并配置环境变量" -ForegroundColor Red
    Read-Host "按任意键退出"
    exit 1
}

try {
    $nodeVersion = node --version 2>&1
    Write-Host "[√] Node.js $nodeVersion 已就绪" -ForegroundColor Green
} catch {
    Write-Host "[×] 未找到 Node.js，请确认已安装 Node.js 并配置环境变量" -ForegroundColor Red
    Read-Host "按任意键退出"
    exit 1
}

Write-Host ""

# 启动后端 (Spring Boot)
Write-Host "[1/2] 正在启动后端服务..." -ForegroundColor Cyan
$BackendProcess = Start-Process -FilePath "cmd.exe" -ArgumentList "/k", "cd /d `"$ProjectDir`" && echo [后端] Spring Boot 启动中... && mvn spring-boot:run" -WindowStyle Normal

# 启动前端 (Vite)
Write-Host "[2/2] 正在启动前端服务..." -ForegroundColor Cyan
$FrontendProcess = Start-Process -FilePath "cmd.exe" -ArgumentList "/k", "cd /d `"$ProjectDir`" && echo [前端] Vite 开发服务器启动中... && npm run dev" -WindowStyle Normal

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  启动完成！两个服务正在各自窗口运行" -ForegroundColor Cyan
Write-Host "  后端: http://localhost:8080"          -ForegroundColor Green
Write-Host "  前端: http://localhost:3000"          -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "关闭本窗口不会影响前后端服务。"       -ForegroundColor Gray
Write-Host "要停止服务，请分别关闭后端和前端窗口。" -ForegroundColor Gray
Write-Host ""

Read-Host "按任意键退出"
