#!/bin/sh

R_LIBS_USER=`Rscript -e 'cat(Sys.getenv("R_LIBS_USER"))'`
JRI="${HOME}${R_LIBS_USER#\~}/rJava/jri"
R_HOME=`R RHOME`
export R_HOME
RHOME=`R RHOME`
export RHOME

D=`dirname "$0"`
if [ `arch` = 'i686' ] ; then 
    OPENGL="${D}/lib/opengl/linux32"
else
    OPENGL="${D}/lib/opengl/linux64"
fi
java -cp "$D" "-Djava.library.path=${JRI}:${OPENGL}" \
    -jar "${D}/Tuner-assembly-0.2.jar" "$@"


