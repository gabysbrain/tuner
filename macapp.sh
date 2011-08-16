#!/bin/sh

set -e

VERSION=0.9
DEPLOYDIR=target/macosx
SHELLNAME=Tuner.app
APPSHELL=macosx/${SHELLNAME}
ASSEMBLY=target/Tuner-assembly-${VERSION}.jar
ICON=icon/tuner_icon.icns
JOGLDIR=lib/opengl/macosx

# This packs up everything for the mac and puts a Tuner.app in target/macosx

./sbt.sh assembly 

rm -rf ${DEPLOYDIR}
mkdir ${DEPLOYDIR}

# Copy the necessary files
echo "copying resources"

cp -Rv ${APPSHELL} ${DEPLOYDIR}/
cp -v ${ASSEMBLY} ${DEPLOYDIR}/${SHELLNAME}/Contents/Resources/Java/
cp -v ${ICON} ${DEPLOYDIR}/${SHELLNAME}/Contents/Resources/
find ${JOGLDIR} -name '*.jnilib' -print -exec cp '{}' ${DEPLOYDIR}/${SHELLNAME}/Contents/Resources/Java/ \;

echo "done"

