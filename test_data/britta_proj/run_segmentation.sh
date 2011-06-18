#!/bin/sh

locations=$1
output=$2
imagedir=$3
startcount=$4

MYDIR=`dirname $0`
AMIRABINDIR=/Users/tom/Downloads/ZIBAmira-2011.01-rc14
AMIRADATADIR=/Users/tom/Downloads/testnetwork
AMIRA=${AMIRABINDIR}/bin/start

cp ${locations} ${AMIRADATADIR}/params.csv

# run amira
cd ${AMIRADATADIR}
${AMIRA} -no_gui -log logfile runAll.hx

# some post-processing is needed
cat output.csv | sed '1d' | sed '/^$/d' | sed -e 's/nan/1/' | sed -e 's/,$//' > ${output}


