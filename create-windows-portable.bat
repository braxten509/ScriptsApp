@echo off
REM Create a truly portable Windows application

echo Creating portable Windows application...
echo.

REM Create output directory
if not exist windows-portable mkdir windows-portable

REM Copy the JAR file
echo Copying JAR file...
copy target\doTERRAApp20-2.0.0.jar windows-portable\

REM Create a Windows launcher that uses system Java
echo Creating Windows launcher...
(
echo @echo off
echo REM doTERRA App Launcher - Works on any Windows PC with Java
echo.
echo REM Hide the console window and run with javaw
echo start "doTERRA App" /B javaw -jar "%%~dp0doTERRAApp20-2.0.0.jar" %%*
echo.
echo if %%errorlevel%% neq 0 ^(
echo     REM If javaw fails, try with java and show error
echo     java -version ^>nul 2^>^&1
echo     if %%errorlevel%% neq 0 ^(
echo         msg %%username%% "Java 17 or higher is required. Please install from https://adoptium.net/"
echo         start https://adoptium.net/
echo     ^) else ^(
echo         REM Try running with console to see error
echo         java -jar "%%~dp0doTERRAApp20-2.0.0.jar" %%*
echo         pause
echo     ^)
echo ^)
) > windows-portable\doTERRA-App.bat

REM Create a more robust VBScript launcher
echo Creating VBScript launcher...
(
echo ' doTERRA App Launcher for Windows
echo ' Works on any Windows PC with Java installed
echo.
echo Set objShell = CreateObject^("WScript.Shell"^)
echo Set objFSO = CreateObject^("Scripting.FileSystemObject"^)
echo.
echo ' Get script directory
echo scriptDir = objFSO.GetParentFolderName^(WScript.ScriptFullName^)
echo jarFile = scriptDir ^& "\doTERRAApp20-2.0.0.jar"
echo.
echo ' Check if JAR exists
echo If Not objFSO.FileExists^(jarFile^) Then
echo     MsgBox "Cannot find doTERRAApp20-2.0.0.jar in " ^& scriptDir, vbCritical, "doTERRA App"
echo     WScript.Quit
echo End If
echo.
echo ' Try to run with javaw ^(no console^)
echo On Error Resume Next
echo objShell.Run "javaw -jar """ ^& jarFile ^& """", 0, False
echo.
echo If Err.Number ^<^> 0 Then
echo     ' Java might not be installed or in PATH
echo     Err.Clear
echo     ' Try java with console
echo     result = objShell.Run^("java -version", 0, True^)
echo     
echo     If result ^<^> 0 Or Err.Number ^<^> 0 Then
echo         answer = MsgBox^("Java 17 or higher is required." ^& vbCrLf ^& vbCrLf ^& _
echo                        "Would you like to download Java now?", _
echo                        vbYesNo + vbExclamation, "doTERRA App - Java Required"^)
echo         If answer = vbYes Then
echo             objShell.Run "https://adoptium.net/"
echo         End If
echo     Else
echo         ' Java exists but javaw failed, try java
echo         objShell.Run "java -jar """ ^& jarFile ^& """", 1, False
echo     End If
echo End If
) > windows-portable\doTERRA-App.vbs

REM Create a PowerShell launcher for modern Windows
echo Creating PowerShell launcher...
(
echo # doTERRA App PowerShell Launcher
echo $ErrorActionPreference = 'Stop'
echo.
echo $scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
echo $jarFile = Join-Path $scriptPath "doTERRAApp20-2.0.0.jar"
echo.
echo # Check if JAR exists
echo if ^(-not ^(Test-Path $jarFile^)^) {
echo     [System.Windows.MessageBox]::Show^(
echo         "Cannot find doTERRAApp20-2.0.0.jar",
echo         "doTERRA App",
echo         [System.Windows.MessageBoxButton]::OK,
echo         [System.Windows.MessageBoxImage]::Error
echo     ^)
echo     exit 1
echo }
echo.
echo # Try to run with javaw
echo try {
echo     Start-Process javaw -ArgumentList "-jar", """$jarFile""" -WindowStyle Hidden
echo } catch {
echo     # Check if Java is installed
echo     try {
echo         $null = java -version 2^>^&1
echo         # Java exists but javaw failed, use java
echo         Start-Process java -ArgumentList "-jar", """$jarFile"""
echo     } catch {
echo         $result = [System.Windows.MessageBox]::Show^(
echo             "Java 17 or higher is required.`nWould you like to download it now?",
echo             "doTERRA App - Java Required",
echo             [System.Windows.MessageBoxButton]::YesNo,
echo             [System.Windows.MessageBoxImage]::Warning
echo         ^)
echo         if ^($result -eq [System.Windows.MessageBoxResult]::Yes^) {
echo             Start-Process "https://adoptium.net/"
echo         }
echo     }
echo }
) > windows-portable\doTERRA-App.ps1

REM Create an HTA launcher (works on all Windows without restrictions)
echo Creating HTA launcher...
(
echo ^<html^>
echo ^<head^>
echo ^<title^>doTERRA App Launcher^</title^>
echo ^<HTA:APPLICATION
echo     ID="doTERRALauncher"
echo     APPLICATIONNAME="doTERRA App Launcher"
echo     BORDER="none"
echo     CAPTION="no"
echo     SHOWINTASKBAR="no"
echo     SINGLEINSTANCE="yes"
echo     SYSMENU="no"
echo     WINDOWSTATE="minimize"^>
echo ^</head^>
echo ^<script language="VBScript"^>
echo Sub Window_OnLoad
echo     window.resizeTo 0, 0
echo     window.moveTo -100, -100
echo     
echo     Set objShell = CreateObject^("WScript.Shell"^)
echo     Set objFSO = CreateObject^("Scripting.FileSystemObject"^)
echo     
echo     strPath = objFSO.GetParentFolderName^(document.location.pathname^)
echo     strPath = Replace^(strPath, "file:///", ""^)
echo     strPath = Replace^(strPath, "/", "\"^)
echo     strPath = Replace^(strPath, "%%20", " "^)
echo     
echo     strJar = strPath ^& "\doTERRAApp20-2.0.0.jar"
echo     
echo     On Error Resume Next
echo     objShell.Run "javaw -jar """ ^& strJar ^& """", 0, False
echo     
echo     If Err.Number ^<^> 0 Then
echo         objShell.Run "java -jar """ ^& strJar ^& """", 1, False
echo     End If
echo     
echo     window.close
echo End Sub
echo ^</script^>
echo ^<body^>^</body^>
echo ^</html^>
) > windows-portable\doTERRA-App.hta

REM Create README
echo Creating README...
(
echo doTERRA App - Portable Windows Version
echo =====================================
echo.
echo This folder contains everything needed to run doTERRA App on any Windows PC.
echo.
echo Requirements:
echo - Java 17 or higher must be installed
echo - Download from: https://adoptium.net/
echo.
echo To Run:
echo - Double-click any of these files:
echo   * doTERRA-App.vbs ^(recommended - no console window^)
echo   * doTERRA-App.bat ^(shows console if there are errors^)
echo   * doTERRA-App.hta ^(works on restricted systems^)
echo   * doTERRA-App.ps1 ^(PowerShell - may require execution policy change^)
echo.
echo To Install on Another PC:
echo 1. Copy this entire 'windows-portable' folder to the other PC
echo 2. Make sure Java 17+ is installed on that PC
echo 3. Double-click doTERRA-App.vbs to run
echo.
echo Creating Desktop Shortcut:
echo 1. Right-click doTERRA-App.vbs
echo 2. Select "Create shortcut"
echo 3. Move the shortcut to your desktop
echo.
echo Troubleshooting:
echo - If nothing happens, run doTERRA-App.bat to see error messages
echo - Make sure Java is installed and in system PATH
echo - Try running as Administrator if needed
) > windows-portable\README.txt

echo.
echo =========================================
echo Portable Windows application created!
echo =========================================
echo.
echo Location: windows-portable\
echo.
echo This folder contains:
echo - doTERRAApp20-2.0.0.jar (the application)
echo - doTERRA-App.vbs (recommended launcher)
echo - doTERRA-App.bat (console launcher)
echo - doTERRA-App.hta (restricted system launcher)
echo - doTERRA-App.ps1 (PowerShell launcher)
echo - README.txt (instructions)
echo.
echo To use on another PC:
echo 1. Copy the entire 'windows-portable' folder to the other PC
echo 2. Ensure Java 17+ is installed on that PC
echo 3. Double-click doTERRA-App.vbs
echo.
echo The VBS file provides the best experience (no console window).
echo.
pause