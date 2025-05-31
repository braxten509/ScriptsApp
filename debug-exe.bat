@echo off
REM Debug script to test the executable

echo Debugging doTERRA App executable...
echo.

REM Navigate to the app folder
cd /d "%~dp0exe-output\doTERRA-App"

if not exist doTERRA-App.exe (
    echo ERROR: doTERRA-App.exe not found!
    echo Please run create-exe.bat first.
    pause
    exit /b 1
)

echo Found doTERRA-App.exe
echo.
echo Checking for runtime folder...

if exist runtime (
    echo Runtime folder found
) else (
    echo ERROR: Runtime folder missing!
    echo The app won't work without it.
)

echo.
echo Trying to run with console output...
doTERRA-App.exe

echo.
echo Exit code: %errorlevel%

if %errorlevel% neq 0 (
    echo.
    echo The app failed to start. Common issues:
    echo 1. Missing runtime folder (must be in same directory as .exe)
    echo 2. Missing app folder with JAR files
    echo 3. Antivirus blocking execution
    echo.
    echo Trying direct Java launch from app folder...
    if exist app\doTERRAApp20-2.0.0.jar (
        echo Found JAR file, attempting direct launch...
        runtime\bin\java.exe -jar app\doTERRAApp20-2.0.0.jar
    ) else (
        echo ERROR: JAR file not found in app folder
    )
)

pause