::BATCH file for windows

set BATDIR=%~dp0
cd %BATDIR%\..

gradlew build packages
