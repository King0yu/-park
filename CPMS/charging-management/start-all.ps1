# 小区停车场管理系统 - 一键启动脚本 (PowerShell)
# 用法: 右键 "使用 PowerShell 运行" 或在项目目录执行 .\start-all.ps1

$ErrorActionPreference = "Stop"
$Host.UI.RawUI.WindowTitle = "停车场管理系统 - 一键启动"

$ProjectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ProjectDir

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  小区停车场管理系统 - 启动中..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "  后端地址: http://localhost:8080" -ForegroundColor Green
Write-Host "  前端地址: http://localhost:3000" -ForegroundColor Green
Write-Host ""

# ============================================
# 环境检查
# ============================================
Write-Host "[检查] 正在检查运行环境..." -ForegroundColor Cyan

# 检查 Java
$javaCmd = "java"
try {
    $javaVersion = & java -version 2>&1 | Select-Object -First 1
    Write-Host "  [√] $javaVersion" -ForegroundColor Green
} catch {
    if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\java.exe")) {
        $javaCmd = "$env:JAVA_HOME\bin\java.exe"
        $javaVersion = & $javaCmd -version 2>&1 | Select-Object -First 1
        Write-Host "  [√] $javaVersion (via JAVA_HOME)" -ForegroundColor Green
    } else {
        Write-Host "  [×] Java 未找到！请安装 JDK 8+ 并配置环境变量。" -ForegroundColor Red
        Write-Host ""
        Read-Host "按任意键退出"
        exit 1
    }
}

# 检查 Node.js
try {
    $nodeVersion = & node --version 2>&1
    Write-Host "  [√] Node.js $nodeVersion" -ForegroundColor Green
} catch {
    Write-Host "  [×] Node.js 未找到！请安装 Node.js 并配置环境变量。" -ForegroundColor Red
    Write-Host ""
    Read-Host "按任意键退出"
    exit 1
}

# 检查端口占用
Write-Host ""
Write-Host "[检查] 正在检查端口占用..." -ForegroundColor Cyan
$port8080 = netstat -ano 2>$null | Select-String ":8080 "
$port3000 = netstat -ano 2>$null | Select-String ":3000 "

if ($port8080) {
    Write-Host "  [!] 端口 8080 已被占用，后端可能已在运行" -ForegroundColor Yellow
} else {
    Write-Host "  [√] 端口 8080 可用" -ForegroundColor Green
}
if ($port3000) {
    Write-Host "  [!] 端口 3000 已被占用，前端可能已在运行" -ForegroundColor Yellow
} else {
    Write-Host "  [√] 端口 3000 可用" -ForegroundColor Green
}

# 检查 Maven 是否首次运行
$m2Wrapper = "$env:USERPROFILE\.m2\wrapper\dists"
if (-not (Test-Path $m2Wrapper)) {
    Write-Host ""
    Write-Host "[mvnw] 首次运行将自动下载 Maven 3.9.9（约 10MB），请耐心等待..." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  启动服务" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# ============================================
# 启动后端 (Spring Boot + Maven Wrapper)
# ============================================
Write-Host "[1/2] 启动后端服务 (Spring Boot, 端口 8080)..." -ForegroundColor Cyan
Write-Host ""
$backendArgs = @(
    "/k",
    "cd /d `"$ProjectDir`"",
    "&",
    "title [后端] 停车场管理系统-8080",
    "&",
    "echo.",
    "&",
    "echo  ====================================",
    "&",
    "echo   后端服务启动中...",
    "&",
    "echo   端口: 8080",
    "&",
    "echo  ====================================",
    "&",
    "echo.",
    "&",
    "echo   [mvnw] 首次运行将自动下载 Maven（约 10MB）",
    "&",
    "echo.",
    "&",
    "mvnw.cmd spring-boot:run",
    "&",
    "pause"
)
$backendCmd = [string]::Join(" ", $backendArgs)
Start-Process -FilePath "cmd.exe" -ArgumentList $backendCmd -WindowStyle Normal

Write-Host "  等待后端初始化 (首次启动需下载 Maven，请耐心等待)..." -ForegroundColor Gray
Write-Host ""

# ============================================
# 启动前端 (Vite)
# ============================================
Write-Host "[2/2] 启动前端服务 (Vite, 端口 3000)..." -ForegroundColor Cyan
Write-Host ""
$frontendArgs = @(
    "/k",
    "cd /d `"$ProjectDir`"",
    "&",
    "title [前端] 停车场管理系统-3000",
    "&",
    "echo.",
    "&",
    "echo  ====================================",
    "&",
    "echo   前端服务启动中...",
    "&",
    "echo   端口: 3000",
    "&",
    "echo  ====================================",
    "&",
    "echo.",
    "&",
    "npm run dev"
)
$frontendCmd = [string]::Join(" ", $frontendArgs)
Start-Process -FilePath "cmd.exe" -ArgumentList $frontendCmd -WindowStyle Normal

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  启动完成！两个服务正在各自窗口运行" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "  后端: http://localhost:8080" -ForegroundColor Green
Write-Host "  前端: http://localhost:3000" -ForegroundColor Green
Write-Host ""
Write-Host "  [提示] 首次启动后端会自动下载 Maven，请在后端窗口查看进度。" -ForegroundColor Yellow
Write-Host ""
Write-Host "  关闭本窗口 不影响 前后端服务。" -ForegroundColor Gray
Write-Host "  请分别关闭后端/前端窗口来停止对应服务。" -ForegroundColor Gray
Write-Host ""

Read-Host "按任意键退出"
