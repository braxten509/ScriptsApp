@echo off
REM Interactive setup script for building portable doTERRA App

echo ============================================
echo doTERRA App Portable Setup
echo ============================================
echo.
echo This script will help you create a portable version
echo of doTERRA App that runs on any Windows PC.
echo.
pause

REM Check for Maven
echo Checking for Maven...
mvn -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Maven not found!
    echo Please install Maven first: https://maven.apache.org/
    pause
    exit /b 1
)

REM Build the application first
echo.
echo Building doTERRA App...
call mvn clean package -DskipTests
if errorlevel 1 (
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo ============================================
echo Build successful!
echo ============================================
echo.
echo Now we need to download JRE and JavaFX.
echo This will create a fully portable app.
echo.
echo Required downloads:
echo 1. OpenJDK 17 JRE (about 45 MB)
echo 2. JavaFX 17 SDK (about 40 MB)
echo.
echo Total portable app size will be about 200 MB.
echo.
choice /c YN /m "Do you want to continue"
if errorlevel 2 goto :END

REM Create download instructions
echo.
echo ============================================
echo DOWNLOAD INSTRUCTIONS
echo ============================================
echo.
echo Please download the following files:
echo.
echo 1. OpenJDK 17 JRE:
echo    https://adoptium.net/temurin/releases/
echo    - Choose: OpenJDK 17 (LTS)
echo    - Operating System: Windows
echo    - Architecture: x64
echo    - Package Type: JRE
echo    - Download the .zip file
echo.
echo 2. JavaFX 17 SDK:
echo    https://openjfx.io/
echo    - Click "Download"
echo    - Choose: JavaFX 17 (LTS)
echo    - Type: SDK
echo    - OS: Windows
echo    - Architecture: x64
echo    - Download the SDK zip
echo.
echo 3. Extract both ZIP files to:
echo    - JRE to: jre-17
echo    - JavaFX to: javafx-17
echo.
echo Press any key to open the download pages...
pause >nul

start https://adoptium.net/temurin/releases/?version=17
start https://gluonhq.com/products/javafx/

echo.
echo After downloading and extracting, press any key to continue...
pause >nul

REM Check if files are extracted
if not exist jre-17 (
    echo ERROR: jre-17 folder not found!
    echo Please extract the JRE to a folder named 'jre-17'
    pause
    exit /b 1
)

if not exist javafx-17 (
    echo ERROR: javafx-17 folder not found!
    echo Please extract JavaFX SDK to a folder named 'javafx-17'
    pause
    exit /b 1
)

REM Now run the build script
echo.
echo Files found! Building portable distribution...
echo.
call build-portable.bat

echo.
echo ============================================
echo Setup Complete!
echo ============================================
echo.
echo Your portable app is in: doTERRA-Portable
echo.
echo To use on any Windows PC:
echo 1. Copy the entire 'doTERRA-Portable' folder
echo 2. Run doTERRA.vbs or doTERRA.bat
echo 3. No installation needed!
echo.

:END
pause