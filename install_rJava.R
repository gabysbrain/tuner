#!/usr/bin/env Rscript
#
# install_rJava.R
#
r_libs_user <- Sys.getenv("R_LIBS_USER")
dir.create(r_libs_user, FALSE, TRUE)
repo <- "http://cran.us.r-project.org"
package <- "rJava"
install.packages(package, lib=r_libs_user, repos=repo)
