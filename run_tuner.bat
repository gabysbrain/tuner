
REM set R_HOME=\\freesia\userdata$\ttorsney\Documents\R\R-2.13.0
set R_HOME=C:\Program Files\R\R-2.13.0
set R_PKG=C:\Users\tom\Documents\R\win-library\2.13
set JRI=%R_PKG%\rJava\jri\x64;%R_PKG%\rJava\jri

set PATH=%PATH%;%R_HOME%\bin;%R_HOME%\bin\x64

set JAVAOPTS=-Djava.library.path=%JRI%

java -Xmx2G %JAVAOPTS% -jar tuner_2.8.1-0.9-onejar.jar
