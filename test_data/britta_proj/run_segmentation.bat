
set locations=%1%
set output=%2%
set imagedir=%3%
set startcount=%4%

set AMIRABINDIR=/Users/tom/Downloads/ZIBAmira-2011.01-rc14
set AMIRADATADIR=/Users/tom/Downloads/testnetwork
set AMIRA=%AMIRABINDIR%\bin\start

awk '{print $1 "," $3 "," $2}' FS="," %locations% > %AMIRADATADIR%\params.csv

REM run amira
cd %AMIRADATADIR%
%AMIRA% -no_gui -log logfile runAll.hx

REM some post-processing is needed
type output.csv | sed '1d' | sed '/^$/d' | sed -e 's/nan/1/' | sed -e 's/,$//' > %output%


