@echo off
REM Script to create Windows executable for doTERRA App
REM This script should be run on Windows with Java 17+ installed

echo Creating Windows executable for doTERRA App...

REM Check if Java is available
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 17 or higher from: https://adoptium.net/
    pause
    exit /b 1
)

REM Check if jpackage is available (Java 14+)
jpackage --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: jpackage is not available
    echo Please ensure you're using Java 17 or higher
    pause
    exit /b 1
)

REM Create the executable
echo Building executable...
jpackage ^
    --input target ^
    --name "doTERRA-App" ^
    --main-jar doTERRAApp20-2.0.0.jar ^
    --main-class com.doterra.app.DoTerraApp ^
    --type exe ^
    --dest exe-output ^
    --app-version 2.0.0 ^
    --description "doTERRA Scripts Application for customer service representatives" ^
    --vendor "doTERRA Scripts" ^
    --win-console ^
    --win-dir-chooser ^
    --win-menu ^
    --win-shortcut

if %errorlevel% equ 0 (
    echo.
    echo SUCCESS: Executable created successfully!
    echo Location: exe-output\doTERRA-App-2.0.0.exe
    echo.
    echo You can now:
    echo 1. Install the application using the installer
    echo 2. Find it in your Start Menu
    echo 3. Run it from anywhere on your system
    echo.
) else (
    echo ERROR: Failed to create executable
)

pause