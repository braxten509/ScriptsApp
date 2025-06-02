@echo off
REM Wrapper to run PowerShell build script and keep window open

echo ============================================
echo doTERRA App Portable Builder
echo ============================================
echo.
echo This will download and set up everything needed
echo to create a portable version of doTERRA App.
echo.
echo Requirements:
echo - Internet connection for downloads
echo - About 500 MB free disk space
echo - Maven installed and in PATH
echo.
pause

REM Check if PowerShell execution policy allows scripts
powershell -Command "Get-ExecutionPolicy" | findstr /i "Restricted" >nul
if %errorlevel%==0 (
    echo.
    echo Setting PowerShell execution policy...
    powershell -Command "Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser -Force"
)

REM Run the PowerShell script
echo.
echo Starting build process...
echo.
powershell -ExecutionPolicy Bypass -File build-portable.ps1

REM Check if build was successful
if exist doTERRA-Portable (
    echo.
    echo ============================================
    echo Build completed successfully!
    echo ============================================
    echo.
    echo The portable app is in: doTERRA-Portable
    echo.
    echo To run on any Windows PC:
    echo 1. Copy the entire 'doTERRA-Portable' folder
    echo 2. Double-click 'doTERRA.vbs' or 'doTERRA.bat'
    echo.
) else (
    echo.
    echo ============================================
    echo Build failed or was cancelled.
    echo ============================================
    echo.
    echo Please check the error messages above.
    echo.
    echo Common issues:
    echo - No internet connection
    echo - Maven not installed
    echo - Antivirus blocking downloads
    echo.
)

pause