#!/usr/bin/Rscript

args <- commandArgs(TRUE)
in.file <- args[1]
out.file <- args[2]

calc.borehole <- function(d) {
  num <- 2 * pi * d$Tu * (d$Hu - d$Hl)
  log.ratio <- log(d$r/d$rw)
  den <- log.ratio * (1 + (2*d$L*d$Tu)/(log.ratio*d$rw**2*d$Kw) + d$Tu/d$Tl)
  num / den
}

in.data <- read.csv(in.file)
out.data <- in.data
out.data$flow <- calc.borehole(in.data)
write.csv(out.data, out.file, row.names=FALSE)

