
set R_HOME=\Library\Frameworks\R.framework\Resources
set JRI=${R_HOME}\library\rJava\jri

set JAVAOPTS=-Djava.library.path=lib\processing\opengl;${JRI}

java ${JAVAOPTS} -jar tuner_2.8.1-0.9-onejar.jar

