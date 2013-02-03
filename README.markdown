# Tuner #

Tuner is an application for finding optimal parameter settings for complex
algorithms.  It supports sampling, resampling, and optimizaing withing a 
high-dimensional parameter space.  It also allows the user to visually see 
the stability of certaint parameter settings with respect to the objective
measures.  This is especially important in cases where one desires an optimum
that is invariant to parameter settings (so things stay good over a wide range
of parameter settings).

## File layout ##

The Tuner deployment files consist of the following:

    .
    | -- run_tuner.bat
    | -- run_tuner.mac.sh
    | -- run_tuner.linux.sh
    | -- Tuner-assembly-0.2.jar
    | -- lib/
         | -- opengl/
              | -- linux32/
              | -- linux64/
              | -- macosx/
              | -- windows32/
              | -- windows64/

run_tuner.bat
:  Tuner startup script for windows

run_tuner.mac.sh
:  Tuner startup script for mac

run_tuner.linux.sh
:  Tuner startup script for linux

Tuner-assembly-0.2.jar
:  The one and only jar file needed

lib/opengl
:  These contain the opengl linking libraries for various architectures.  
   Make sure that java.library.path is pointing to the right one.

## Run dependencies ##

* Java (version 6 tested)
* R (64-bit version) (2.13 and 2.14 tested)
* rJava

The startup scripts take care of setting the R_HOME environment variable,
which is needed for rJava to work.  They also take care up setting
java.library.path to point to the linking opengl libraries and the jri linking
library.  They also start java with 2GB of heap space as the default is really
too small to do anything.

There are also a number of R packages Tuner uses, but it should install them
automatically if it can't find them:

* lhs
* mlegp
* labeling

## Installation ##

Here are prepatory steps to get Tuner up and running:

1. install java
2. install R (make sure you are using the 64-bit version)
3. install the required R packages: type 
   `install.packages(c('rJava', 'mlegp', 'lhs', 'labeling'))`
   at the R prompt.

### Windows ###

Windows stores R package files in different locations depending on the version
of R installed.  Open up the `start_tuner.bat` file and change R_HOME and JRI
appropriately.  JRI should end up in the same place relative to R_HOME. R_HOME
will be something like `C:\Program Files\R\R-2.14.0` on windows.

## Usage ##

There is a pre-built project for testing purposes.  If you can open it and
everything draws correctly then Tuner should be at least mostly working.  Here
is how to open an external project:

1. Click on the 'open other' button in the project browser
   ![](https://bitbucket.org/gabysbrain/tuner/raw/3856d9d1c0aa/doc/images/open_other_button.png)

2. Navigate to test_data and click on the test_proj directory.  Then click
   choose
   ![](https://bitbucket.org/gabysbrain/tuner/raw/3856d9d1c0aa/doc/images/open_test_project.png)

3. A window such as the following should open
   ![](https://bitbucket.org/gabysbrain/tuner/raw/3856d9d1c0aa/doc/images/test_project_viewer.png)

## Build dependencies ##

For building Tuner needs Scala and simple build tool installed.  The sbt
build script should take care of downloading all the required libraries.

