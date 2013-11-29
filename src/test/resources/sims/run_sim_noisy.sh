#!/bin/sh

mydir=`dirname $0`

cat ${mydir}/sim_stdout.log
cat ${mydir}/sim_stderr.log 1>&2

cp ${mydir}/good_output.csv $2

