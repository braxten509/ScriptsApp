' doTERRA App Launcher VBScript
' This provides a silent launcher without console window

Option Explicit

Dim objShell, objFSO, strScriptPath, strJarPath, intResult
Set objShell = CreateObject("WScript.Shell")
Set objFSO = CreateObject("Scripting.FileSystemObject")

' Get the directory where this script is located
strScriptPath = objFSO.GetParentFolderName(WScript.ScriptFullName)

' Define possible JAR locations
Dim arrPaths(2)
arrPaths(0) = strScriptPath & "\doTERRAApp20-2.0.0.jar"
arrPaths(1) = strScriptPath & "\target\doTERRAApp20-2.0.0.jar"
arrPaths(2) = objFSO.GetParentFolderName(strScriptPath) & "\target\doTERRAApp20-2.0.0.jar"

' Find the JAR file
Dim i
strJarPath = ""
For i = 0 To UBound(arrPaths)
    If objFSO.FileExists(arrPaths(i)) Then
        strJarPath = arrPaths(i)
        Exit For
    End If
Next

' Check if JAR was found
If strJarPath = "" Then
    MsgBox "Cannot find doTERRAApp20-2.0.0.jar" & vbCrLf & _
           "Please ensure the JAR file is in the same directory as this launcher.", _
           vbCritical, "doTERRA App - File Not Found"
    WScript.Quit 1
End If

' Check if Java is available
On Error Resume Next
intResult = objShell.Run("java -version", 0, True)
If Err.Number <> 0 Or intResult <> 0 Then
    intResult = MsgBox("Java 17 or higher is required to run doTERRA App." & vbCrLf & vbCrLf & _
                      "Would you like to download Java now?", _
                      vbYesNo + vbExclamation, "doTERRA App - Java Required")
    If intResult = vbYes Then
        objShell.Run "https://adoptium.net/"
    End If
    WScript.Quit 1
End If
On Error GoTo 0

' Run the application without showing console window
objShell.Run "javaw -jar """ & strJarPath & """", 0, False

' Clean up
Set objShell = Nothing
Set objFSO = Nothing