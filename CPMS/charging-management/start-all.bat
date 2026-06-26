@echo off
chcp 65001 >nul
title 停车场管理系统 - 一键启动

set "PROJECT_DIR=%~dp0"
cd /d "%PROJECT_DIR%"

echo ========================================
echo   停车场管理系统 - 正在启动前后端服务
echo ========================================
echo.
echo 后端地址: http://localhost:8080
echo 前端地址: http://localhost:3000
echo.
echo [提示] 请确保 MySQL 服务已启动
echo.

:: 启动后端 (Spring Boot)
echo [1/2] 正在启动后端服务...
start "后端-8080" cmd /k "cd /d "%PROJECT_DIR%" && echo [后端] Maven 编译启动中... && mvn spring-boot:run"

:: 等待后端先开始编译
echo [2/2] 正在启动前端服务...
start "前端-3000" cmd /k "cd /d "%PROJECT_DIR%" && echo [前端] Vite 开发服务器启动中... && npm run dev"

echo.
echo ========================================
echo   启动完成！两个服务正在各自窗口运行
echo   后端: http://localhost:8080
echo   前端: http://localhost:3000
echo ========================================
echo.
echo 关闭本窗口不会影响前后端服务。
echo 要停止服务，请分别关闭"后端-8080"和"前端-3000"窗口。
echo.

pause
