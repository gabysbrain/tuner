#!/bin/sh

R_LIBS_USER=`Rscript -e 'cat(Sys.getenv("R_LIBS_USER"))'`
JRI="${HOME}${R_LIBS_USER#\~}/rJava/jri"
R_HOME=`R RHOME`
export R_HOME

git clone https://bitbucket.org/gabysbrain/date.scala.git date-scala

java -Xmx512M "-Djava.library.path=$JRI" -jar sbt-launch.jar assembly \
	&& mv target/Tuner-assembly-0.9.jar  .


