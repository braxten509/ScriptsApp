# doTERRA App 2.0 - Portable Windows Edition

## Overview
This guide explains how to create a fully portable version of doTERRA App that can run on any Windows PC without requiring Java or JavaFX installation.

## Quick Start

### Option 1: Using PowerShell (Recommended)
```powershell
# Run in PowerShell as Administrator
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
.\build-portable.ps1
```

This will automatically:
- Download OpenJDK 17 JRE
- Download JavaFX 17
- Build the application
- Create a portable folder
- Generate a distribution ZIP

### Option 2: Manual Setup
1. Run `setup-portable.bat` and follow the instructions
2. Download the required files when prompted
3. The script will build the portable version

### Option 3: Complete Manual Build
1. Download [OpenJDK 17 JRE](https://adoptium.net/temurin/releases/?version=17) (Windows x64 ZIP)
2. Download [JavaFX 17 SDK](https://openjfx.io/) (Windows x64 SDK)
3. Extract JRE to `jre-17` folder
4. Extract JavaFX to `javafx-17` folder
5. Run `build-portable.bat`

## Distribution Structure

```
doTERRA-Portable/
├── doTERRA.exe          # Launch4j executable (if created)
├── doTERRA.bat          # Batch launcher (console)
├── doTERRA.vbs          # Silent launcher (no console)
├── README.txt           # User instructions
├── app/
│   └── doTERRAApp20-2.0.0.jar
├── jre/                 # Embedded Java Runtime
│   └── bin/
│       └── java.exe
├── javafx/              # JavaFX libraries
│   ├── *.jar
│   └── *.dll
└── data/                # User data (portable)
    ├── doterra_chat_buttons.dat
    ├── doterra_email_buttons.dat
    ├── sticky_notes.dat
    └── ...
```

## Features

### Complete Portability
- No installation required
- No admin rights needed
- Runs from USB drives
- Self-contained Java runtime
- All dependencies included

### Data Portability
- All user data stored in `data` folder
- Settings travel with the app
- No registry entries
- No files in user profile

## System Requirements

- Windows 7 or later (64-bit)
- ~200 MB free disk space
- No Java installation required

## Usage Instructions

### For End Users
1. Copy the `doTERRA-Portable` folder to any location
2. Double-click `doTERRA.vbs` to start
3. All your data is saved in the `data` subfolder

### For USB/Portable Drives
1. Copy entire folder to USB drive
2. Run directly from USB on any Windows PC
3. Data stays on the USB drive

## Build Requirements

To build the portable version yourself:
- Maven 3.6+
- Internet connection (for downloads)
- PowerShell or Command Prompt
- ~500 MB temporary space

## Troubleshooting

### App won't start
- Ensure Windows 7+ 64-bit
- Check antivirus exceptions
- Run `doTERRA.bat` to see errors
- Verify all folders are present

### "Java not found" error
- Ensure `jre` folder exists
- Check `jre/bin/java.exe` is present
- Folder structure must be intact

### JavaFX errors
- Ensure `javafx` folder has all JARs
- Check for missing DLL files
- Verify module-path is correct

## Creating a Smaller Distribution

To reduce size:
1. Use jlink to create custom JRE
2. Remove unnecessary JavaFX modules
3. Use UPX to compress executables
4. ZIP with maximum compression

## Advanced Options

### Launch4j Executable
To create a native Windows EXE:
1. Download [Launch4j](http://launch4j.sourceforge.net/)
2. Use `launch4j-config.xml`
3. Build executable
4. Place in portable folder

### Custom Icon
1. Create/obtain a 256x256 icon
2. Save as `doTERRA.ico`
3. Update Launch4j config
4. Rebuild executable

### Silent Updates
The portable version can be updated by:
1. Replacing the JAR file
2. Keeping the same filename
3. Data files remain unchanged

## Security Notes

- No installation = no admin rights needed
- All files are local
- No network communication required
- Antivirus may flag unknown EXE

## Distribution

### For IT Departments
- Deploy via network share
- No MSI/installer needed
- Easy to update centrally
- No conflicts with Java versions

### For Individual Users
- Download and extract
- No technical knowledge required
- Works immediately
- Fully self-contained

## License

This portable distribution includes:
- OpenJDK (GPL v2 with Classpath Exception)
- JavaFX (GPL v2 with Classpath Exception)
- doTERRA App (Your license)

## Support

For issues with:
- Portable build process: Check this README
- Application bugs: See main README
- Distribution: Verify file integrity