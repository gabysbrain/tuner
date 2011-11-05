# Tuner #

Tuner is an application for finding optimal parameter settings for complex
algorithms.  It supports sampling, resampling, and optimizaing withing a 
high-dimensional parameter space.  It also allows the user to visually see 
the stability of certaint parameter settings with respect to the objective
measures.  This is especially important in cases where one desires an optimum
that is invariant to parameter settings (so things stay good over a wide range
of parameter settings).

## Dependencies ##

* Java
* R (64-bit version)

There are also a number of R packages Tuner uses, but it should install them
automatically:
* rJava
* lhs
* mlegp
* labeling

For building Tuner needs Scala and simple build tool installed.  The sbt
build script should take care of downloading all the required libraries.

## Installation ##

1. install java
2. install R (make sure you are using the 64-bit version)
3. install the required R packages: lhs, mlegp, rJava, and labeling. To do 
   this you can type , for example, install.packages('labeling') at the 
   R prompt to install the labeling package.
4. Open up the start_tuner.bat file and change R_HOME and JRI appropriately. 
   JRI should end up in the same place relative to R_HOME. I think R_HOME 
   should be something like 'C:\Program Files\R\R-2.10.0' on windows.


