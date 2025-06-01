# Running doTERRA App on Windows

## Quick Start - Just Run It!

**Easiest method:**
1. Double-click `RUN-DOTERRA-APP.bat`
2. That's it!

**Requirements:**
- Java 17 or higher installed
- Download Java from: https://adoptium.net/

## Creating a Real .exe File

### Use Launch4j (Recommended)

1. Download Launch4j: http://launch4j.sourceforge.net/
2. Follow the instructions in `CREATE-EXE-WITH-LAUNCH4J.md`
3. Creates a real .exe that works anywhere

## If Nothing Works

1. **Check Java is installed:**
   ```
   java -version
   ```
   Should show version 17 or higher

2. **Try running directly:**
   ```
   java -jar target\doTERRAApp20-2.0.0.jar
   ```

3. **Common fixes:**
   - Install Java from https://adoptium.net/
   - Run as Administrator
   - Check antivirus isn't blocking
   - Make sure you have the `target` folder with the JAR file

## For Other PCs

To run on another computer:
1. Copy the entire project folder
2. Make sure Java 17+ is installed on that PC
3. Double-click `RUN-DOTERRA-APP.bat`