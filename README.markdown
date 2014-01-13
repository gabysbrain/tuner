# Tuner [![Build Status](https://travis-ci.org/gabysbrain/tuner.png?branch=master)](https://travis-ci.org/gabysbrain/tuner)

Tuner is an application for finding optimal parameter settings for complex
algorithms.  It supports sampling, resampling, and optimizaing withing a 
high-dimensional parameter space.  It also allows the user to visually see 
the stability of certaint parameter settings with respect to the objective
measures.  This is especially important in cases where one desires an optimum
that is invariant to parameter settings (so things stay good over a wide range
of parameter settings).

## Requirements

Tuner requires Java (version 6 and 7 tested).

## Installation

A tar.gz file of the Tuner package is located 
[here](https://github.com/gabysbrain/tuner/releases/download/v0.10.0/Tuner-0.10.0.tar.gz).
Download this and unpack it.  There is a `Makefile` within the archive that 
will install Tuner and its associated files to `~/local`.

    tar xzf Tuner-0.10.0.tar.gz
    cd Tuner-0.10.0
    make install

If you would like to install Tuner somewhere else then you can run

    make install PREFIX="/usr/local"

## Usage

To run Tuner there is a shell script, `bin/tuner` (for Unix/Mac) and
`bin/tuner.bat` (for Windows).
The startup scripts take care of set up java with 6GB of heap space as the
default is really too small to do anything.  You can adjust this using
the `JAVA_OPTS` environment variable.

There is a pre-built project for testing purposes that you can download
[here](https://raw.github.com/gabysbrain/tuner/master/test_data/test_proj.tar.gz).
Unpack this file somewhere. If you can open it and
everything draws correctly then Tuner should be at least mostly working.  Here
is how to open an external project:

1. Click on the 'open other' button in the project browser
   ![](https://raw.github.com/gabysbrain/tuner/master/doc/images/open_other_button.png)

2. Navigate to test_data and click on the test_proj directory.  Then click
   choose
   ![](https://raw.github.com/gabysbrain/tuner/master/doc/images/open_test_project.png)

3. A window such as the following should open
   ![](https://raw.github.com/gabysbrain/tuner/master/doc/images/test_project_viewer.png)

## Black box scripts

See the wiki for more detailed information at <https://github.com/gabysbrain/tuner/wiki/SamplingScript>.

The scripts needs to take 2 command line arguments: The first argument is a
file path to an input csv file, which is where Tuner will place the sample
point locations.  The second argument is the output file path where Tuner
expects the result csv file to be written.  The output csv file must contain
both the sample locations (i.e. the same columns as in the input file) as well
as the assessment results (dice coefficients, volume errors, etc.) from the
segmentation.

The CSV files use standard US/international format, i.e. ',' as separator, '.'
as decimal point, and the first line contains comma-separated column headers.

## Build dependencies

Tuner is written in Scala.  For building it uses 
[simple build tool](http://www.scala-sbt.org) installed.  The sbt
build script should take care of downloading all the required libraries.

