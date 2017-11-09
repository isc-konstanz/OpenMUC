::BATCH file for windows

call gradlew.bat -b framework/conf/bundles.conf.gradle updateBundles
call gradlew.bat -b ../emonmuc/conf/bundles.conf.gradle updateBundles
pause
