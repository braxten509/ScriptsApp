# Creating doTERRA App .exe with Launch4j

## Step 1: Download Launch4j

1. Go to: http://launch4j.sourceforge.net/
2. Download the Windows version
3. Install it (or use the portable version)

## Step 2: Create the .exe

1. Open Launch4j
2. Use these settings:

### Basic Tab:
- **Output file**: `C:\path\to\doTERRA-App.exe`
- **Jar**: Browse to `target\doTERRAApp20-2.0.0.jar`
- **Icon**: (optional) Add an .ico file if you have one

### JRE Tab:
- **Min JRE version**: `17`
- **Prefer public JRE**: Check this
- **Initial heap size**: `128`
- **Max heap size**: `1024`

### Click the Build wrapper button (gear icon)

## Step 3: Test the .exe

The created .exe file:
- Can be moved anywhere
- Will find Java on the system
- Shows proper error if Java is missing
- Works on any Windows PC

## Alternative: Simple Batch Solution

If Launch4j doesn't work, use the included `RUN-DOTERRA-APP.bat`:

1. Copy these files to any folder:
   - `RUN-DOTERRA-APP.bat`
   - `target` folder (containing the JAR)

2. Double-click `RUN-DOTERRA-APP.bat`

## What You Need on Other PCs

- Java 17 or higher installed
- The .exe file (if using Launch4j)
- OR the .bat file + target folder (if using batch)

## Download Links

- **Launch4j**: http://launch4j.sourceforge.net/
- **Java 17**: https://adoptium.net/
- **Alternative Java**: https://www.oracle.com/java/technologies/downloads/