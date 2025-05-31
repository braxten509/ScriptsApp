# Creating Windows Executable for doTERRA App

This guide will help you create a Windows executable (.exe) file for the doTERRA App that can be run from anywhere on your computer.

## Prerequisites

1. **Java 17 or higher** must be installed on your Windows machine
   - Download from: https://adoptium.net/
   - Make sure it's added to your system PATH

2. **The JAR file** must be built (already done: `target/doTERRAApp20-2.0.0.jar`)

## Method 1: Automated Script (Recommended)

1. Open Command Prompt as Administrator
2. Navigate to this project directory
3. Run the batch script:
   ```cmd
   create-exe.bat
   ```

This will create:
- An installer: `exe-output/doTERRA-App-2.0.0.exe`
- Start Menu shortcuts
- Desktop shortcut (optional during install)

## Method 2: Manual jpackage Command

Open Command Prompt in this directory and run:

```cmd
jpackage --input target --name "doTERRA-App" --main-jar doTERRAApp20-2.0.0.jar --main-class com.doterra.app.DoTerraApp --type exe --dest exe-output --app-version 2.0.0 --description "doTERRA Scripts Application" --vendor "doTERRA Scripts" --win-console --win-dir-chooser --win-menu --win-shortcut
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