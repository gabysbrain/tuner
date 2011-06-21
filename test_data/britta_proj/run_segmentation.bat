
set locations=%1
set output=%2
set imagedir=%3
set startcount=%4

set AMIRABINDIR=C:\Program Files\ZIBAmira
set AMIRADATADIR=C:\Users\tom\Downloads\testnetwork
set AMIRA=%AMIRABINDIR%\bin\start

awk '{print $1 "," $3 "," $2}' FS="," %locations% > %AMIRADATADIR%\params.csv

REM run amira
cd %AMIRADATADIR%
%AMIRA% -no_gui -log logfile runAll.hx

REM some post-processing is needed
C:\cygwin\bin\cat output.csv | C:\cygwin\bin\sed '1d' | C:\cygwin\bin\sed '/^$/d' | C:\cygwin\bin\sed -e 's/nan/1/' | C:\cygwin\bin\sed -e 's/,$//' > %output%


