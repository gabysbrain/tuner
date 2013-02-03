#!/bin/sh

JRI=/Library/Frameworks/R.framework/Versions/Current/Resources/library/rJava/jri
export R_HOME=/Library/Frameworks/R.framework/Resources
OPENGL=lib/opengl/macosx

java -Djava.library.path=${JRI}:${OPENGL} -jar Tuner-assembly-0.2.jar

