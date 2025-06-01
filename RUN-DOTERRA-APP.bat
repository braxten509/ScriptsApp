@echo off
echo Starting doTERRA App...
echo.

REM Try to run the JAR directly
java -jar "%~dp0target\doTERRAApp20-2.0.0.jar"

REM If that fails, show error
if %errorlevel% neq 0 (
    echo.
    echo ==========================================
    echo ERROR: Could not start doTERRA App
    echo ==========================================
    echo.
    echo Possible issues:
    echo 1. Java is not installed
    echo 2. JAR file is not in target folder
    echo.
    echo To fix:
    echo 1. Install Java from: https://adoptium.net/
    echo 2. Make sure doTERRAApp20-2.0.0.jar is in the target folder
    echo.
    pause
)