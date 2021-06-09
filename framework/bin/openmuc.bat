::BATCH file for windows
@echo off
set RUNDIR=%~dp0..
set ROOTDIR=%~dp0..\..
set CURRENTDIR=%cd%

call %ROOTDIR%\gradlew.bat -p %ROOTDIR% framework --warning-mode all

cd %RUNDIR%\
java -jar felix\felix.jar

cd %CURRENTDIR%
@echo on
