::BATCH file for windows

call gradlew.bat build
call gradlew.bat -b framework/conf/bundles.conf.gradle updateBundles
pause
