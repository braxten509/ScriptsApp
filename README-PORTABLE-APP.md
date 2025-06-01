# doTERRA App - Portable Windows Application

## Quick Start

1. Run `create-windows-portable.bat`
2. Copy the entire `windows-portable` folder to any Windows PC
3. Double-click `doTERRA-App.vbs` to run

## What You Get

The `windows-portable` folder contains:
- **doTERRAApp20-2.0.0.jar** - The application
- **doTERRA-App.vbs** - Silent launcher (recommended)
- **doTERRA-App.bat** - Console launcher (for troubleshooting)
- **doTERRA-App.hta** - Alternative launcher for restricted systems
- **doTERRA-App.ps1** - PowerShell launcher

## System Requirements

- **Windows 7 or higher**
- **Java 17 or higher** installed
  - Download from: https://adoptium.net/
  - Or: https://www.oracle.com/java/technologies/downloads/

## Why the jpackage App Didn't Work

The app created by jpackage on WSL/Linux includes a Linux runtime, not Windows. That's why it doesn't work on Windows. To create a proper Windows executable, you need to:

1. Run jpackage on Windows (not WSL)
2. OR use our portable launchers (recommended)
3. OR use Launch4j on Windows

## Making it Work on Different PCs

### Option 1: Portable Folder (Recommended)
- Copy the entire `windows-portable` folder
- Requires Java to be installed on target PC
- Works immediately, no installation needed

### Option 2: Bundle Java (No Java Required)
Download a Windows JRE and include it:

1. Download Windows JRE from https://adoptium.net/
2. Extract it to `windows-portable/jre/`
3. Modify the launchers to use bundled Java:

```batch
@echo off
REM Check for bundled Java first
if exist "%~dp0jre\bin\javaw.exe" (
    "%~dp0jre\bin\javaw.exe" -jar "%~dp0doTERRAApp20-2.0.0.jar"
) else (
    REM Fall back to system Java
    javaw -jar "%~dp0doTERRAApp20-2.0.0.jar"
)
```

### Option 3: Create Installer on Windows
Use these tools on a Windows machine:
- **Launch4j**: Creates exe that bundles or finds Java
- **Inno Setup**: Creates professional installer
- **NSIS**: Creates compact installer

## File Associations

To run from anywhere:
1. Add the `windows-portable` folder to your PATH
2. OR create shortcuts to `doTERRA-App.vbs`
3. OR use Windows "Send to" menu

## Troubleshooting

**Nothing happens when double-clicking:**
- Run `doTERRA-App.bat` to see error messages
- Check if Java is installed: `java -version` in Command Prompt
- Try right-click → "Run as Administrator"

**"Java not found" error:**
- Install Java from https://adoptium.net/
- Restart computer after installation
- Ensure Java is in system PATH

**Works on dev PC but not others:**
- Ensure you copied the ENTIRE folder
- Check Java version: needs Java 17+
- Disable antivirus temporarily (may block .vbs files)

## Creating a True Single .exe

To convert the VBS launcher to a single .exe:

1. **Using IExpress (Built into Windows):**
   ```
   1. Run: iexpress
   2. Create new Self Extraction Directive
   3. Add doTERRA-App.vbs and doTERRAApp20-2.0.0.jar
   4. Set doTERRA-App.vbs as install program
   ```

2. **Using Bat To Exe Converter:**
   - Download from: https://bat-to-exe-converter.software.informer.com/
   - Convert the .bat file with "Invisible" option

3. **Using VbsEdit:**
   - Download from: http://www.vbsedit.com/
   - Has built-in VBS to EXE compiler