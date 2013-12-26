#!/bin/sh

D=`dirname "$0"`/target
java -Xmx6G -jar "${D}/Tuner-assembly-0.10.0.jar" "$@"


