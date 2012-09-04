#!/bin/sh

export R_HOME=/Library/Frameworks/R.framework/Resources
JRI=/Library/Frameworks/R.framework/Versions/Current/Resources/library/rJava/jri

java -Xmx512M -Djava.library.path=${JRI} -jar sbt-launch.jar $*

