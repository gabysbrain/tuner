#!/bin/sh

sbt clean scoverage:test coveralls

