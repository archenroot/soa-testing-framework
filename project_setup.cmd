@echo off

set LINK=C:\SOATestingFramework

if exist "%LINK%" goto OK
echo Creating symbolic link "C:\SOATestingFramework" pointing to the project's root...
mklink /D C:\SOATestingFramework .

:OK
echo.
echo Please make sure that the property user.properties.file in .\nbproject\private\private.properties
echo is pointing to the correct directory on your environment, e.g.:
echo user.properties.file=C:\\Users\\IBM_ADMIN\\AppData\\Roaming\\NetBeans\\7.4\\build.properties
echo.
pause