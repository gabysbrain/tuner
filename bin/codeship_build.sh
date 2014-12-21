#!/bin/sh

mydir=`dirname ${0}`
${mydir}/sbt clean scoverage:test coveralls

(cd ${mydir}/gui; npm test)
