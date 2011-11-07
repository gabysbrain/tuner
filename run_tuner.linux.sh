#!/bin/sh

JRI=/home/tom/R/x86_64-redhat-linux-gnu-library/2.13/rJava/jri
export R_HOME=/usr/lib64/R
OPENGL=lib/opengl/linux64

java -Djava.library.path=${JRI}:${OPENGL} -jar Tuner-assembly-0.9.jar

