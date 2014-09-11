
@ECHO OFF

SET MYDIR=%~dp0

TYPE %MYDIR%\sim_stdout.log
TYPE %MYDIR%\sim_stderr.log 1>&2

COPY %MYDIR%\good_output.csv %2
