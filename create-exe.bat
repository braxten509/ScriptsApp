@echo off
REM Script to create Windows executable for doTERRA App
REM This script should be run on Windows with Java 17+ installed

echo Creating Windows executable for doTERRA App...
echo.

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

echo Choose creation method:
echo 1. Create MSI installer (requires WiX Toolset)
echo 2. Create portable app image (no installer, just .exe)
echo 3. Create both
echo.
set /p choice="Enter your choice (1-3): "

if "%choice%"=="1" goto create_msi
if "%choice%"=="2" goto create_app
if "%choice%"=="3" goto create_both
echo Invalid choice!
pause
exit /b 1

:create_msi
echo.
echo Creating MSI installer...
echo NOTE: This requires WiX Toolset from https://wixtoolset.org
jpackage ^
    --input target ^
    --name "doTERRA-App" ^
    --main-jar doTERRAApp20-2.0.0.jar ^
    --main-class com.doterra.app.DoTerraApp ^
    --type msi ^
    --dest exe-output ^
    --app-version 2.0.0 ^
    --description "doTERRA Scripts Application for customer service representatives" ^
    --vendor "doTERRA Scripts" ^
    --win-dir-chooser ^
    --win-menu ^
    --win-shortcut ^
    --win-shortcut-prompt

if %errorlevel% equ 0 (
    echo SUCCESS: MSI installer created!
    echo Location: exe-output\doTERRA-App-2.0.0.msi
) else (
    echo ERROR: Failed to create MSI installer
    echo Make sure WiX Toolset is installed and in PATH
)
goto end

:create_app
echo.
echo Creating portable application...
jpackage ^
    --input target ^
    --name "doTERRA-App" ^
    --main-jar doTERRAApp20-2.0.0.jar ^
    --main-class com.doterra.app.DoTerraApp ^
    --type app-image ^
    --dest exe-output ^
    --app-version 2.0.0 ^
    --description "doTERRA Scripts Application" ^
    --vendor "doTERRA Scripts"

if %errorlevel% equ 0 (
    echo SUCCESS: Portable application created!
    echo Location: exe-output\doTERRA-App\doTERRA-App.exe
    echo.
    echo You can:
    echo 1. Run the .exe directly from exe-output\doTERRA-App\
    echo 2. Copy the entire doTERRA-App folder anywhere
    echo 3. Create shortcuts to doTERRA-App.exe
) else (
    echo ERROR: Failed to create portable application
)
goto end

:create_both
call :create_app
echo.
call :create_msi
goto end

:end
echo.
pause