
# This script needs to be run from Rscript

library('mlegp')

#################################
# Functions
#################################

# This generates n samples in each dimension and 
# returns all possible combinations
generate.trials <- function(data, n=20) {
  # create a list of samples
  dim.samples <- vector("list", ncol(data))
  for(c in 1:ncol(data)) {
    dim.samples[[c]] <- seq(min(data[,c]), max(data[,c]), length.out=n)
  }
  trials <- data.frame(do.call('expand.grid', dim.samples))

  # put the names back
  names(trials) <- names(data)

  return(trials)
}

##' Read in a design table
##'
##' @param fname The filename to read
##' @return A data table with field names from the given file
read.design <- function(fname) {
  return(read.csv(fname))
}

##' Takes the log of an arbitrary column saving the 
##' result in the same data table with the name log.colname.
##'
##' @param data The data table
##' @param colname The name of the column of which to take the log
log.column <- function(data, colname) {
  newcol <- paste("log.", colname, sep="")
  data[,newcol] <- log(data[,colname])
  return(data)
}

##' Saves a data table out in csv format 
##' that the mrsparkle program can read
##'
##' @param data The data table to save
##' @param fname The filename to save as
save.table <- function(data, fname) {
  write.table(data, file=fname, quote=FALSE, sep=",", row.names=FALSE)
}

##' Builds a gp model
##'
##' @param design The data table containing the design sites
##' @param param.cols A vector of strings which indicate which 
##'                   columns we want to use to build the gp model
##' @param resp.col A string for which column to 
##'                 consider as the response measure
##' @return A list object containing the model we've built
fit.model <- function(design, param.cols, resp.col) {
  X.samples <- design[,param.cols]
  Y.samples <- design[,resp.col]

  # now build the gp model
  fit <- mlegp(as.matrix(X.samples), as.vector(Y.samples),
               param.names=param.cols, gp.names=resp.col, constantMean=0)

  print(names(X.samples))
  print(fit$beta)

  return(fit)
}

##' Evaluates a gp model at a number of points spaced 
##' regularly the design table used to 
##' originally build the model
##'
##' @param gp A built gp model
##' @param num.samples An integer indicating how many
##'                    samples in each dimension to try
##' @return A data table containing the sampled points
##'         and a column called se.fit that contains 
##'         the estimated standard error
take.estimates <- function(fit, num.samples) {
  response.field <- 'dice'
  X.trials <- generate.trials(data.frame(fit$X), n=num.samples)
  Y.trials <- predict(fit, as.matrix(X.trials), se.fit=TRUE)

  # put all the trial data into a table
  trial.data <- data.frame(dace_stddev=Y.trials$se.fit)
  trial.data[,response.field] <- Y.trials$fit
  for(c in 1:ncol(X.trials)) {
    trial.data[,names(X.trials)[c]] <- X.trials[,c]
  }
  
  return(trial.data)
}

take.estimates2 <- function(fit, min.max.locations, num.samples) {
  response.field <- 'dice'
  X.trials <- generate.trials(min.max.locations, n=num.samples)
  Y.trials <- predict(fit, as.matrix(X.trials), se.fit=TRUE)

  # put all the trial data into a table
  trial.data <- data.frame(dace_stddev=Y.trials$se.fit)
  trial.data[,response.field] <- Y.trials$fit
  for(c in 1:ncol(X.trials)) {
    trial.data[,names(X.trials)[c]] <- X.trials[,c]
  }
  
  return(trial.data)
}

