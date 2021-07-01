#!/bin/sh
#Scriptname: build.sh
#Description: script to build OpenMUC debian packages with dpkg

if [ $(id -u) != 0 ]; then
    echo "DPKG build process should be performed with root privileges." 1>&2
    exit 1
fi

find_openmuc() {
    # Attempt to set OPENMUC_HOME
    # Resolve links: $0 may be a link
    PRG="$0"
    # Need this for relative symlinks.
    while [ -h "$PRG" ] ; do
        ls=`ls -ld "$PRG"`
        link=`expr "$ls" : '.*-> \(.*\)$'`
        if expr "$link" : '/.*' > /dev/null; then
            PRG="$link"
        else
            PRG=`dirname "$PRG"`"/$link"
        fi
    done
    SAVED="`pwd`"
    cd "`dirname \"$PRG\"`/.." >/dev/null
    OPENMUC_HOME="`pwd -P`"
    cd "$SAVED" >/dev/null
}

find_gradle() {
    if hash gradle 2>/dev/null; then
        GRADLE="gradle"
    else
        GRADLE=$OPENMUC_HOME"/gradlew"
    fi
}

find_openmuc
find_gradle

cd "$OPENMUC_HOME"

eval $GRADLE packages

for package in build/dpkg/*/ ; do
    cd $package
    chmod 755 $package/debian/pre* 2>/dev/null
    chmod 755 $package/debian/post* 2>/dev/null
    chmod 755 $package/debian/rules

    dpkg-buildpackage -us -uc
done
exit 0
