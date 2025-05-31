@echo off
REM Simple launcher for doTERRA App

REM Check if Java is available
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 17 or higher from: https://adoptium.net/
    pause
    exit /b 1
)

REM Get the directory where this script is located
set SCRIPT_DIR=%~dp0

REM Change to script directory
cd /d "%SCRIPT_DIR%"

REM Launch the application
echo Starting doTERRA App...
java -jar target\doTERRAApp20-2.0.0.jar

REM If the JAR fails, it might be in current directory
if %errorlevel% neq 0 (
    if exist doTERRAApp20-2.0.0.jar (
        java -jar doTERRAApp20-2.0.0.jar
    ) else (
        echo ERROR: Cannot find doTERRAApp20-2.0.0.jar
        echo Please ensure the JAR file is in the target\ directory or current directory
        pause
    )
)