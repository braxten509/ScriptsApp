# PowerShell script to build a fully portable doTERRA App for Windows
# This creates a self-contained folder with embedded JRE and JavaFX

param(
    [string]$OutputDir = "doTERRA-Portable"
)

# Error handling
$ErrorActionPreference = "Stop"
trap {
    Write-Host "`nError occurred: $_" -ForegroundColor Red
    Write-Host "`nPress any key to exit..." -ForegroundColor Yellow
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    exit 1
}

Write-Host "Building doTERRA App Portable Distribution..." -ForegroundColor Green

# Clean previous build
if (Test-Path $OutputDir) {
    Write-Host "Cleaning previous build..." -ForegroundColor Yellow
    Remove-Item -Path $OutputDir -Recurse -Force
}

# Create directory structure
Write-Host "Creating directory structure..." -ForegroundColor Yellow
New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null
New-Item -ItemType Directory -Path "$OutputDir\app" -Force | Out-Null
New-Item -ItemType Directory -Path "$OutputDir\jre" -Force | Out-Null
New-Item -ItemType Directory -Path "$OutputDir\javafx" -Force | Out-Null
New-Item -ItemType Directory -Path "$OutputDir\data" -Force | Out-Null

# Build the application
Write-Host "Building application with Maven..." -ForegroundColor Yellow
mvn clean package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "Maven build failed!" -ForegroundColor Red
    exit 1
}

# Copy the JAR
Write-Host "Copying application JAR..." -ForegroundColor Yellow
Copy-Item "target\doTERRAApp20-2.0.0.jar" "$OutputDir\app\"

# Download JRE if not exists
$jreUrl = "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.13%2B11/OpenJDK17U-jre_x64_windows_hotspot_17.0.13_11.zip"
$jreZip = "openjdk-17-jre.zip"

if (-not (Test-Path $jreZip)) {
    Write-Host "Downloading OpenJDK 17 JRE..." -ForegroundColor Yellow
    Write-Host "This may take a few minutes..." -ForegroundColor Gray
    try {
        Invoke-WebRequest -Uri $jreUrl -OutFile $jreZip -UseBasicParsing
    } catch {
        Write-Host "Failed to download JRE. Please download manually from:" -ForegroundColor Red
        Write-Host $jreUrl -ForegroundColor Cyan
        exit 1
    }
}

# Extract JRE
Write-Host "Extracting JRE..." -ForegroundColor Yellow
try {
    Expand-Archive -Path $jreZip -DestinationPath "$OutputDir\jre" -Force
    # Move contents up one directory level
    $jreSubDir = Get-ChildItem -Path "$OutputDir\jre" -Directory | Select-Object -First 1
    if ($jreSubDir) {
        Get-ChildItem -Path $jreSubDir.FullName -Force | Move-Item -Destination "$OutputDir\jre" -Force
        Remove-Item $jreSubDir.FullName -Force -Recurse
    }
} catch {
    Write-Host "Failed to extract JRE: $_" -ForegroundColor Red
    throw
}

# Download JavaFX if not exists
$javafxUrl = "https://download2.gluonhq.com/openjfx/17.0.13/openjfx-17.0.13_windows-x64_bin-sdk.zip"
$javafxZip = "openjfx-17.zip"

if (-not (Test-Path $javafxZip)) {
    Write-Host "Downloading JavaFX 17..." -ForegroundColor Yellow
    Write-Host "This may take a few minutes..." -ForegroundColor Gray
    try {
        Invoke-WebRequest -Uri $javafxUrl -OutFile $javafxZip -UseBasicParsing
    } catch {
        Write-Host "Failed to download JavaFX. Please download manually from:" -ForegroundColor Red
        Write-Host $javafxUrl -ForegroundColor Cyan
        exit 1
    }
}

# Extract JavaFX
Write-Host "Extracting JavaFX..." -ForegroundColor Yellow
try {
    Expand-Archive -Path $javafxZip -DestinationPath "temp-javafx" -Force
    # Copy only the necessary files
    $javafxDir = Get-ChildItem -Path "temp-javafx" -Directory -Filter "javafx-sdk-*" | Select-Object -First 1
    if ($javafxDir) {
        Copy-Item "$($javafxDir.FullName)\lib\*" "$OutputDir\javafx\" -Force
        Copy-Item "$($javafxDir.FullName)\bin\*" "$OutputDir\javafx\" -Force -ErrorAction SilentlyContinue
    } else {
        throw "JavaFX SDK directory not found in extracted files"
    }
    Remove-Item "temp-javafx" -Recurse -Force
} catch {
    Write-Host "Failed to extract JavaFX: $_" -ForegroundColor Red
    if (Test-Path "temp-javafx") {
        Remove-Item "temp-javafx" -Recurse -Force -ErrorAction SilentlyContinue
    }
    throw
}

# Create launcher batch file
Write-Host "Creating launcher..." -ForegroundColor Yellow
$launcherContent = @'
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

REM Launch the application
"%JRE_HOME%\bin\java.exe" ^
    --module-path "%JAVAFX_PATH%" ^
    --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.swing,javafx.media ^
    -Dprism.order=sw ^
    -Dprism.verbose=true ^
    -Djava.library.path="%JAVAFX_PATH%" ^
    -jar "%APP_JAR%"

if errorlevel 1 (
    echo.
    echo Application exited with an error.
    pause
)
'@

$launcherContent | Out-File -FilePath "$OutputDir\doTERRA.bat" -Encoding ASCII

# Create VBS launcher for no-console window
Write-Host "Creating silent launcher..." -ForegroundColor Yellow
$vbsContent = @'
Set objShell = CreateObject("Wscript.Shell")
objShell.CurrentDirectory = Left(WScript.ScriptFullName, InStrRev(WScript.ScriptFullName, "\") - 1)
objShell.Run "doTERRA.bat", 0, False
'@

$vbsContent | Out-File -FilePath "$OutputDir\doTERRA.vbs" -Encoding ASCII

# Create README
Write-Host "Creating documentation..." -ForegroundColor Yellow
$readmeContent = @"
doTERRA App 2.0 - Portable Edition
==================================

This is a fully self-contained version of doTERRA App that includes:
- OpenJDK 17 JRE (Java Runtime)
- JavaFX 17 Libraries
- doTERRA App 2.0

REQUIREMENTS
------------
- Windows 7 or later (64-bit)
- No installation required!

HOW TO RUN
----------
Option 1: Double-click 'doTERRA.vbs' (recommended - no console window)
Option 2: Double-click 'doTERRA.bat' (shows console window)

FEATURES
--------
- Chat Scripts Management
- Email Scripts with HTML Editor
- Todo List with Reminders
- Multiple Sticky Notes
- Calculator with Regex
- Variable Templates
- Drag & Drop Interface

DATA STORAGE
------------
All your data is stored in the 'data' folder:
- Chat scripts: data/doterra_chat_buttons.dat
- Email scripts: data/doterra_email_buttons.dat
- Sticky notes: data/sticky_notes.dat
- Todo items: data/doterra_todos.dat
- Regex templates: data/regex_templates.dat

PORTABILITY
-----------
To use on another computer:
1. Copy the entire 'doTERRA-Portable' folder to a USB drive
2. Run from the USB drive on any Windows PC
3. Your data in the 'data' folder will travel with the app

TROUBLESHOOTING
---------------
If the app doesn't start:
1. Make sure you're running Windows 7 or later (64-bit)
2. Try running doTERRA.bat to see error messages
3. Ensure the folder structure is intact
4. Check that antivirus isn't blocking the app

Version: 2.0.0
Built: $(Get-Date -Format "yyyy-MM-dd")
"@

$readmeContent | Out-File -FilePath "$OutputDir\README.txt" -Encoding UTF8

# Create a simple icon if possible (optional)
Write-Host "Creating application icon..." -ForegroundColor Yellow
$iconContent = @'
# doTERRA App Icon
# Place a proper icon file here named doTERRA.ico
'@
$iconContent | Out-File -FilePath "$OutputDir\doTERRA.ico.txt" -Encoding ASCII

# Copy existing data files if they exist
Write-Host "Checking for existing data files..." -ForegroundColor Yellow
if (Test-Path "data") {
    Write-Host "Copying existing data files..." -ForegroundColor Yellow
    Copy-Item "data\*" "$OutputDir\data\" -Force -ErrorAction SilentlyContinue
}

# Create ZIP file for distribution
$zipFile = "doTERRA-Portable.zip"
Write-Host "Creating distribution ZIP..." -ForegroundColor Yellow
if (Test-Path $zipFile) {
    Remove-Item $zipFile -Force
}
Compress-Archive -Path $OutputDir -DestinationPath $zipFile -CompressionLevel Optimal

# Summary
Write-Host "`n========================================" -ForegroundColor Green
Write-Host "Build Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host "Portable folder: $OutputDir" -ForegroundColor Cyan
Write-Host "Distribution ZIP: $zipFile" -ForegroundColor Cyan
Write-Host "`nTo use on any Windows PC:" -ForegroundColor Yellow
Write-Host "1. Copy the '$OutputDir' folder or extract $zipFile" -ForegroundColor White
Write-Host "2. Run doTERRA.vbs (no console) or doTERRA.bat (with console)" -ForegroundColor White
Write-Host "3. No Java or JavaFX installation required!" -ForegroundColor White

# Calculate folder size
$folderSize = (Get-ChildItem $OutputDir -Recurse | Measure-Object -Property Length -Sum).Sum / 1MB
Write-Host "`nTotal size: $([math]::Round($folderSize, 2)) MB" -ForegroundColor Gray

# Keep window open
Write-Host "`nPress any key to exit..." -ForegroundColor Yellow
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")