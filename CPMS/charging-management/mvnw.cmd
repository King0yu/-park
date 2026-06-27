@REM ----------------------------------------------------------------------------
@REM Maven Wrapper startup script for Windows
@REM ----------------------------------------------------------------------------
@echo off
chcp 65001 >nul 2>&1
setlocal enabledelayedexpansion

set "BASE_DIR=%~dp0"
if "%BASE_DIR:~-1%"=="\" set "BASE_DIR=%BASE_DIR:~0,-1%"
set "WRAPPER_JAR=%BASE_DIR%\.mvn\wrapper\maven-wrapper.jar"

if not exist "%WRAPPER_JAR%" (
    echo [ERROR] Maven Wrapper JAR not found: %WRAPPER_JAR%
    pause
    exit /b 1
)

set "JAVA_CMD=java"
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
    )
)

"%JAVA_CMD%" -version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Java not found. Install JDK 8+.
    pause
    exit /b 1
)

"%JAVA_CMD%" ^
  -Xmx1024m ^
  -Dfile.encoding=UTF-8 ^
  -classpath "%WRAPPER_JAR%" ^
  "-Dmaven.multiModuleProjectDirectory=%BASE_DIR%" ^
  org.apache.maven.wrapper.MavenWrapperMain ^
  %*

set "EXIT_CODE=%ERRORLEVEL%"
exit /b %EXIT_CODE%
