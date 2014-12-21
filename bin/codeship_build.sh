#!/bin/sh

mydir=`dirname ${0}`
${mydir}/sbt clean scoverage:test coveralls

(cd gui; npm test)
