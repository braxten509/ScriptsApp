Set objShell = CreateObject("WScript.Shell")
Set objFSO = CreateObject("Scripting.FileSystemObject")

' Get the directory where this script is located
strScriptDir = objFSO.GetParentFolderName(WScript.ScriptFullName)

' Change to script directory
objShell.CurrentDirectory = strScriptDir

' Create a temporary batch file to handle the complex command
strTempBat = strScriptDir & "\temp_launch.bat"
Set objFile = objFSO.CreateTextFile(strTempBat, True)
objFile.WriteLine "@echo off"
objFile.WriteLine "cd /d """ & strScriptDir & """"
objFile.WriteLine """" & strScriptDir & "\jre\bin\javaw.exe"" --module-path """ & strScriptDir & "\javafx"" --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.media -Dprism.order=sw -Djava.library.path=""" & strScriptDir & "\javafx"" -jar """ & strScriptDir & "\app\doTERRAApp20-2.0.0.jar"""
objFile.Close

' Run the batch file hidden
objShell.Run """" & strTempBat & """", 0, False

' Wait a moment then delete the temp file
WScript.Sleep 1000
objFSO.DeleteFile strTempBat
