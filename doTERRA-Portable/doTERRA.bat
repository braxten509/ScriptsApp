@echo off
cd /d "%~dp0"

REM doTERRA App Portable Launcher
REM This is a self-contained version with embedded JRE and JavaFX

REM Set paths relative to this script
set APP_HOME=%~dp0
set JRE_HOME=%APP_HOME%jre
set JAVAFX_PATH=%APP_HOME%javafx
set APP_JAR=%APP_HOME%app\doTERRAApp20-2.0.0.jar

REM Check if required files exist
if not exist "%JRE_HOME%\bin\java.exe" (
    echo Error: JRE not found in %JRE_HOME%
    echo Please ensure the portable package is complete.
    pause
    exit /b 1
)

if not exist "%APP_JAR%" (
    echo Error: Application JAR not found at %APP_JAR%
    pause
    exit /b 1
)

REM Launch the application (removed javafx.swing module)
"%JRE_HOME%\bin\java.exe" ^
    --module-path "%JAVAFX_PATH%" ^
    --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.media ^
    -Dprism.order=sw ^
    -Dprism.verbose=true ^
    -Djava.library.path="%JAVAFX_PATH%" ^
    -jar "%APP_JAR%"

if errorlevel 1 (
    echo.
    echo Application exited with an error.
    pause
)
