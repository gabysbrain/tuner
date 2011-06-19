#!/bin/sh

JRI=/Library/Frameworks/R.framework/Versions/Current/Resources/library/rJava/jri
export R_HOME=/Library/Frameworks/R.framework/Resources

java -Djava.library.path=${JRI} -jar Tuner-assembly-0.9.jar

