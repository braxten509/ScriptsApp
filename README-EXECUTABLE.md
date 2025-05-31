# Creating Windows Executable for doTERRA App

This guide will help you create a Windows executable (.exe) file for the doTERRA App that can be run from anywhere on your computer.

## Prerequisites

1. **Java 17 or higher** must be installed on your Windows machine
   - Download from: https://adoptium.net/
   - Make sure it's added to your system PATH

2. **The JAR file** must be built (already done: `target/doTERRAApp20-2.0.0.jar`)

## Method 1: Simple Launcher (Easiest)

Just double-click `doTERRA-App.bat` to run the application!

This batch file will:
- Check for Java installation
- Launch the application from the JAR file
- Work from any location

## Method 2: Automated Script (Create Portable .exe)

1. Open Command Prompt as Administrator
2. Navigate to this project directory
3. Run the batch script:
   ```cmd
   create-exe.bat
   ```
4. Choose option **2** for "Create portable app image"

This will create:
- A portable application in `exe-output/doTERRA-App/`
- An executable at `exe-output/doTERRA-App/doTERRA-App.exe`
- No installation needed - just run the .exe!

## Method 3: Manual jpackage Command

For a portable app (no installer):
```cmd
jpackage --input target --name "doTERRA-App" --main-jar doTERRAApp20-2.0.0.jar --main-class com.doterra.app.DoTerraApp --type app-image --dest exe-output --app-version 2.0.0
```

For an MSI installer (requires WiX Toolset):
```cmd
jpackage --input target --name "doTERRA-App" --main-jar doTERRAApp20-2.0.0.jar --main-class com.doterra.app.DoTerraApp --type msi --dest exe-output --app-version 2.0.0 --win-dir-chooser --win-menu --win-shortcut
```

## Method 3: Alternative Tools

### Launch4j (GUI Tool)
1. Download Launch4j from: http://launch4j.sourceforge.net/
2. Use these settings:
   - **Output file**: `doTERRA-App.exe`
   - **Jar**: `target/doTERRAApp20-2.0.0.jar`
   - **Main class**: `com.doterra.app.DoTerraApp`
   - **Min JRE version**: `17.0.0`

### JWrapper (Commercial)
1. Download from: https://jwrapper.com/
2. Follow their GUI to wrap the JAR file

## Running the Application

Once created, the executable can be:

1. **Installed system-wide**: Run the installer .exe file
2. **Added to PATH**: Place the exe in a PATH directory
3. **Run from Start Menu**: After installation
4. **Run from Desktop**: Using created shortcuts

## Features Included

✅ **Sticky Notes**: Floating note window that stays on top
✅ **Ctrl+S Hotkey**: Save current button or create new one
✅ **Independent Dialogs**: All dialogs stay on top and work independently
✅ **HTML Content Stripping**: Clean text display in variable dialogs
✅ **All Original Features**: Chat scripts, email scripts, regex editor, calculator, todos

## Troubleshooting

### "Java not found" Error
- Install Java 17+ from https://adoptium.net/
- Add Java to your system PATH

### "jpackage not found" Error
- Ensure you're using Java 17 or higher
- Oracle JDK or OpenJDK both work

### Executable Won't Run
- Right-click the exe and "Run as Administrator"
- Check Windows Defender/antivirus settings
- Ensure all dependencies are included

## File Locations

After installation, the application data will be stored in:
- **Sticky Notes**: `sticky_note.txt` (in app directory)
- **Button Data**: `doterra_chat_buttons.dat`, `doterra_email_buttons.dat`
- **Settings**: Various `.dat` files in the app directory

The application will automatically save all changes and preserve your data between sessions.