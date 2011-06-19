
set R_HOME=\\freesia\userdata$\ttorsney\Documents\R\R-2.13.0
set JRI=%R_HOME%\library\rJava\jri\x64;%R_HOME%\library\rJava\jri

set PATH=%PATH%;%R_HOME%\bin;%R_HOME%\bin\x64

set JAVAOPTS=-Djava.library.path=lib\processing\opengl;%JRI%

java -Xmx2G %JAVAOPTS% -jar tuner_2.8.1-0.9-onejar.jar
