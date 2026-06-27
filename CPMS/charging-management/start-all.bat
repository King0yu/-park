@echo off
chcp 65001 >nul
title 停车场管理系统 - 一键启动

set "PROJECT_DIR=%~dp0"
set "PROJECT_DIR=%PROJECT_DIR:~0,-1%"
cd /d "%PROJECT_DIR%"

echo.
echo ========================================
echo   小区停车场管理系统 - 启动中...
echo ========================================
echo.
echo   后端地址: http://localhost:8080
echo   前端地址: http://localhost:3000
echo.

:: ============================================
:: 环境检查
:: ============================================
echo [检查] 正在检查运行环境...

:: 检查 Java
set "JAVA_OK=0"
java -version >nul 2>&1
if %ERRORLEVEL% equ 0 (
    for /f "tokens=3" %%i in ('java -version 2^>^&1 ^| findstr /i "version"') do echo   [√] Java %%i
    set "JAVA_OK=1"
) else (
    if defined JAVA_HOME (
        if exist "%JAVA_HOME%\bin\java.exe" (
            "%JAVA_HOME%\bin\java.exe" -version >nul 2>&1
            if %ERRORLEVEL% equ 0 echo   [√] Java (via JAVA_HOME)
            set "JAVA_OK=1"
        )
    )
)
if %JAVA_OK% equ 0 (
    echo   [×] Java 未找到！请安装 JDK 8+ 并配置环境变量。
    echo.
    pause
    exit /b 1
)

:: 检查 Node.js
node --version >nul 2>&1
if %ERRORLEVEL% equ 0 (
    for /f %%i in ('node -v') do echo   [√] Node.js %%i
) else (
    echo   [×] Node.js 未找到！请安装 Node.js 并配置环境变量。
    echo.
    pause
    exit /b 1
)

:: 检查端口占用
echo.
echo [检查] 正在检查端口占用...
set "PORT_8080=free"
set "PORT_3000=free"
netstat -ano 2>nul | findstr ":8080 " >nul && set "PORT_8080=occupied"
netstat -ano 2>nul | findstr ":3000 " >nul && set "PORT_3000=occupied"

if "%PORT_8080%"=="occupied" (
    echo   [!] 端口 8080 已被占用，后端可能已在运行
)
if "%PORT_3000%"=="occupied" (
    echo   [!] 端口 3000 已被占用，前端可能已在运行
)
if "%PORT_8080%"=="free" if "%PORT_3000%"=="free" echo   [√] 端口 8080 / 3000 可用

:: 检查 Maven 是否首次运行
if not exist "%USERPROFILE%\.m2\wrapper\dists" (
    echo.
    echo [mvnw] 首次运行将自动下载 Maven 3.9.9，请耐心等待...
    echo.
)

echo.
echo ========================================
echo   启动服务
echo ========================================
echo.

:: ============================================
:: 启动后端 (Spring Boot + Maven Wrapper)
:: ============================================
echo [1/2] 启动后端服务 ^(Spring Boot, 端口 8080^)...
echo.
start "[后端] 停车场管理系统-8080" cmd /k "cd /d "%PROJECT_DIR%" && title [后端] 停车场管理系统-8080 && echo. && echo  ==================================== && echo   后端服务启动中... && echo   端口: 8080 && echo  ==================================== && echo. && echo  [mvnw] 首次运行将自动下载 Maven（约 10MB），后续启动无需再下载 && echo. && mvnw.cmd spring-boot:run && pause"

:: 给后端一点启动时间（首次下载 Maven 可能需要更久）
echo   等待后端初始化 ^(首次启动需下载 Maven，请耐心等待^)...

:: ============================================
:: 启动前端 (Vite)
:: ============================================
echo [2/2] 启动前端服务 ^(Vite, 端口 3000^)...
echo.
start "[前端] 停车场管理系统-3000" cmd /k "cd /d "%PROJECT_DIR%" && title [前端] 停车场管理系统-3000 && echo. && echo  ==================================== && echo   前端服务启动中... && echo   端口: 3000 && echo  ==================================== && echo. && npm run dev"

echo.
echo ========================================
echo   启动完成！
echo ========================================
echo.
echo   后端: http://localhost:8080
echo   前端: http://localhost:3000
echo.
echo   [提示] 首次启动后端会自动下载 Maven，请在后端窗口查看进度。
echo.
echo   关闭本窗口 不影响 前后端服务。
echo   请分别关闭后端/前端窗口来停止对应服务。
echo.
pause
