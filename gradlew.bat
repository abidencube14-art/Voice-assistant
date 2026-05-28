@ECHO OFF

SET DIR=%~dp0

java -version >nul 2>&1
IF %ERRORLEVEL% NEQ 0 (
    ECHO Java not found
    EXIT /B 1
)

java -jar "%DIR%gradle\wrapper\gradle-wrapper.jar" %*
