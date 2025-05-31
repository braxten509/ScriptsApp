@echo off
REM Create a truly portable single-file executable

echo Creating portable doTERRA App executable...
echo.

REM Method 1: Using jpackage with embedded runtime
echo Method 1: Creating self-contained portable app...
echo.

REM First, let's create a simple wrapper that includes everything
if not exist portable-temp mkdir portable-temp

REM Copy the JAR
copy target\doTERRAApp20-2.0.0.jar portable-temp\

REM Create a launcher script that will be compiled
echo Creating launcher script...
(
echo @echo off
echo java -jar "%%~dp0doTERRAApp20-2.0.0.jar" %%*
) > portable-temp\launcher.bat

echo.
echo Option 1: Single-folder portable app
echo =====================================
jpackage ^
    --input target ^
    --name "doTERRA-App-Portable" ^
    --main-jar doTERRAApp20-2.0.0.jar ^
    --main-class com.doterra.app.DoTerraApp ^
    --type app-image ^
    --dest portable-output ^
    --java-options "-Xmx1024m" ^
    --app-version 2.0.0

if %errorlevel% equ 0 (
    echo.
    echo SUCCESS! Portable app created in: portable-output\doTERRA-App-Portable\
    echo.
    echo This folder contains everything needed to run the app.
    echo You can:
    echo 1. Copy the entire folder to any location
    echo 2. Create a shortcut to doTERRA-App-Portable.exe
    echo 3. Add the folder to your PATH
) else (
    echo Failed to create portable app with jpackage
)

echo.
echo Option 2: Creating PowerShell-based portable executable...
echo =========================================================

REM Create a PowerShell script that embeds the JAR and extracts/runs it
echo Creating self-extracting executable script...
(
echo # Self-contained doTERRA App Launcher
echo $jarPath = Join-Path $env:TEMP "doTERRAApp20-2.0.0.jar"
echo.
echo # Check if Java is installed
echo try {
echo     $javaVersion = java -version 2^>^&1
echo     if ^($LASTEXITCODE -ne 0^) { throw }
echo } catch {
echo     [System.Windows.Forms.MessageBox]::Show^("Java 17 or higher is required.`nPlease download from: https://adoptium.net/", "doTERRA App - Java Required", "OK", "Error"^)
echo     Start-Process "https://adoptium.net/"
echo     exit 1
echo }
echo.
echo # Extract and run JAR
echo if ^(-not ^(Test-Path $jarPath^)^) {
echo     Write-Host "Extracting application..."
echo     # In a real implementation, the JAR would be embedded here
echo     # For now, we'll copy from the known location
echo     $scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
echo     $sourceJar = Join-Path $scriptDir "target\doTERRAApp20-2.0.0.jar"
echo     if ^(Test-Path $sourceJar^) {
echo         Copy-Item $sourceJar $jarPath
echo     } else {
echo         [System.Windows.Forms.MessageBox]::Show^("Application file not found!", "Error", "OK", "Error"^)
echo         exit 1
echo     }
echo }
echo.
echo # Run the application
echo Write-Host "Starting doTERRA App..."
echo Start-Process java -ArgumentList "-jar", $jarPath -NoNewWindow -Wait
) > doTERRA-App-Portable.ps1

echo.
echo Option 3: Batch file wrapper (simplest)
echo =======================================

REM Create a robust batch file that can find the JAR
(
echo @echo off
echo setlocal enabledelayedexpansion
echo.
echo REM doTERRA App Portable Launcher
echo REM This script will find and run the JAR file
echo.
echo REM Check Java
echo java -version ^>nul 2^>^&1
echo if %%errorlevel%% neq 0 ^(
echo     echo ERROR: Java is not installed or not in PATH
echo     echo Please install Java 17 or higher from: https://adoptium.net/
echo     echo.
echo     echo Press any key to open the download page...
echo     pause ^>nul
echo     start https://adoptium.net/
echo     exit /b 1
echo ^)
echo.
echo REM Find the JAR file
echo set JAR_NAME=doTERRAApp20-2.0.0.jar
echo set JAR_FOUND=0
echo.
echo REM Check in same directory as this script
echo if exist "%%~dp0%%JAR_NAME%%" ^(
echo     set JAR_PATH=%%~dp0%%JAR_NAME%%
echo     set JAR_FOUND=1
echo ^)
echo.
echo REM Check in target subdirectory
echo if %%JAR_FOUND%%==0 if exist "%%~dp0target\%%JAR_NAME%%" ^(
echo     set JAR_PATH=%%~dp0target\%%JAR_NAME%%
echo     set JAR_FOUND=1
echo ^)
echo.
echo REM Check in parent directory
echo if %%JAR_FOUND%%==0 if exist "%%~dp0..\%%JAR_NAME%%" ^(
echo     set JAR_PATH=%%~dp0..\%%JAR_NAME%%
echo     set JAR_FOUND=1
echo ^)
echo.
echo if %%JAR_FOUND%%==0 ^(
echo     echo ERROR: Cannot find %%JAR_NAME%%
echo     echo Please ensure the JAR file is in the same directory as this script
echo     pause
echo     exit /b 1
echo ^)
echo.
echo REM Run the application
echo echo Starting doTERRA App...
echo java -jar "%%JAR_PATH%%" %%*
echo.
echo if %%errorlevel%% neq 0 ^(
echo     echo.
echo     echo Application exited with error code: %%errorlevel%%
echo     pause
echo ^)
) > doTERRA-App-Portable.bat

echo.
echo ========================================
echo Portable executable options created:
echo.
echo 1. portable-output\doTERRA-App-Portable\ - Full portable app folder
echo 2. doTERRA-App-Portable.ps1 - PowerShell launcher
echo 3. doTERRA-App-Portable.bat - Batch file launcher (RECOMMENDED)
echo.
echo The .bat file is the most reliable and can be placed anywhere.
echo Just keep it in the same folder as the JAR file or in the project root.
echo.

REM Cleanup
if exist portable-temp rmdir /s /q portable-temp

pause