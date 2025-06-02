@echo off
REM Batch file to build portable doTERRA App
REM This is a simpler version that requires manual downloading of JRE and JavaFX

echo ============================================
echo doTERRA App Portable Builder
echo ============================================
echo.

set OUTPUT_DIR=doTERRA-Portable

REM Clean previous build
if exist %OUTPUT_DIR% (
    echo Cleaning previous build...
    rmdir /s /q %OUTPUT_DIR%
)

REM Create directories
echo Creating directory structure...
mkdir %OUTPUT_DIR%
mkdir %OUTPUT_DIR%\app
mkdir %OUTPUT_DIR%\jre
mkdir %OUTPUT_DIR%\javafx
mkdir %OUTPUT_DIR%\data

REM Build the application
echo Building application...
call mvn clean package -DskipTests
if errorlevel 1 (
    echo Maven build failed!
    pause
    exit /b 1
)

REM Copy JAR
echo Copying application JAR...
copy target\doTERRAApp20-2.0.0.jar %OUTPUT_DIR%\app\

REM Check for JRE
if exist jre-17 (
    echo Copying JRE...
    xcopy /s /e /i jre-17\* %OUTPUT_DIR%\jre\
) else (
    echo.
    echo WARNING: JRE not found!
    echo Please download OpenJDK 17 JRE and extract to 'jre-17' folder
    echo Download from: https://adoptium.net/
    echo.
)

REM Check for JavaFX
if exist javafx-17 (
    echo Copying JavaFX...
    xcopy /s /e /i javafx-17\lib\* %OUTPUT_DIR%\javafx\
    if exist javafx-17\bin xcopy /s /e /i javafx-17\bin\* %OUTPUT_DIR%\javafx\
) else (
    echo.
    echo WARNING: JavaFX not found!
    echo Please download JavaFX 17 SDK and extract to 'javafx-17' folder
    echo Download from: https://openjfx.io/
    echo.
)

REM Create launcher
echo Creating launcher...
(
echo @echo off
echo cd /d "%%~dp0"
echo.
echo REM doTERRA App Portable Launcher
echo set APP_HOME=%%~dp0
echo set JRE_HOME=%%APP_HOME%%jre
echo set JAVAFX_PATH=%%APP_HOME%%javafx
echo set APP_JAR=%%APP_HOME%%app\doTERRAApp20-2.0.0.jar
echo.
echo if not exist "%%JRE_HOME%%\bin\java.exe" ^(
echo     echo Error: JRE not found. Please ensure the portable package is complete.
echo     pause
echo     exit /b 1
echo ^)
echo.
echo if not exist "%%APP_JAR%%" ^(
echo     echo Error: Application JAR not found.
echo     pause
echo     exit /b 1
echo ^)
echo.
echo "%%JRE_HOME%%\bin\java.exe" ^^
echo     --module-path "%%JAVAFX_PATH%%" ^^
echo     --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.swing,javafx.media ^^
echo     -Dprism.order=sw ^^
echo     -jar "%%APP_JAR%%"
echo.
echo if errorlevel 1 pause
) > %OUTPUT_DIR%\doTERRA.bat

REM Create VBS launcher
echo Creating silent launcher...
(
echo Set objShell = CreateObject^("Wscript.Shell"^)
echo objShell.CurrentDirectory = Left^(WScript.ScriptFullName, InStrRev^(WScript.ScriptFullName, "\"^) - 1^)
echo objShell.Run "doTERRA.bat", 0, False
) > %OUTPUT_DIR%\doTERRA.vbs

REM Copy data files if exist
if exist data (
    echo Copying data files...
    xcopy /s /e /i data\* %OUTPUT_DIR%\data\
)

REM Create README
echo Creating README...
(
echo doTERRA App 2.0 - Portable Edition
echo ==================================
echo.
echo This portable version includes everything needed to run on any Windows PC.
echo No installation required!
echo.
echo TO RUN:
echo - Double-click doTERRA.vbs ^(no console window^)
echo - Or double-click doTERRA.bat ^(shows console^)
echo.
echo REQUIREMENTS:
echo - Windows 7 or later ^(64-bit^)
echo.
echo PORTABILITY:
echo Copy the entire folder to any Windows PC and run!
echo Your data is stored in the 'data' subfolder.
) > %OUTPUT_DIR%\README.txt

echo.
echo ============================================
echo Build complete!
echo ============================================
echo.
echo Portable folder created: %OUTPUT_DIR%
echo.
echo To distribute:
echo 1. Ensure JRE is in %OUTPUT_DIR%\jre
echo 2. Ensure JavaFX is in %OUTPUT_DIR%\javafx
echo 3. ZIP the entire %OUTPUT_DIR% folder
echo.
pause