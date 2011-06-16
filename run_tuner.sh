#!/bin/sh

JRI=/Library/Frameworks/R.framework/Versions/Current/Resources/library/rJava/jri
export R_HOME=/Library/Frameworks/R.framework/Resources
java -Djava.library.path=${JRI} -jar target/scala-2.8.1/tuner_2.8.1-0.9-onejar.jar $*
