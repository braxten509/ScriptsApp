Set objShell = CreateObject("WScript.Shell")
Set objFSO = CreateObject("Scripting.FileSystemObject")

' Get the directory where this script is located
strScriptPath = WScript.ScriptFullName
strScriptDir = objFSO.GetParentFolderName(strScriptPath)

' Change to script directory
objShell.CurrentDirectory = strScriptDir

' Build the Java command
strJavaExe = strScriptDir & "\jre\bin\javaw.exe"
strJavaFX = strScriptDir & "\javafx"
strAppJar = strScriptDir & "\app\doTERRAApp20-2.0.0.jar"

' Build full command line (removed javafx.swing)
strCommand = """" & strJavaExe & """" & _
    " --module-path """ & strJavaFX & """" & _
    " --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.media" & _
    " -Dprism.order=sw" & _
    " -Djava.library.path=""" & strJavaFX & """" & _
    " -jar """ & strAppJar & """"

' Run the application without showing console window
objShell.Run strCommand, 0, False
