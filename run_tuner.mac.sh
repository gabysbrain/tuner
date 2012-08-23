#!/bin/sh

JRI=/Library/Frameworks/R.framework/Versions/Current/Resources/library/rJava/jri
export R_HOME=/Library/Frameworks/R.framework/Resources
OPENGL=lib/macosx

java -Djava.library.path=${JRI}:${OPENGL} -jar target/Tuner-assembly-0.9.jar

