
set R_HOME=C:\Program Files\R\R-2.14.0
set R_PKG=%HOMEPATH%\Documents\R\win-library\2.14
set JRI=%R_PKG%\rJava\jri\x64;%R_PKG%\rJava\jri
set OPENGL=lib\opengl\windows64

set PATH=%PATH%;%R_HOME%\bin;%R_HOME%\bin\x64;%OPENGL%

set JAVAOPTS=-Djava.library.path=%JRI%;%OPENGL%

java -Xmx2G %JAVAOPTS% -jar Tuner-assembly-0.9.jar
