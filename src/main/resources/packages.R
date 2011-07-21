
required.packages <- c('rJava', 'mlegp', 'lhs', 'labeling')

is.installed <- function(pkg.name) {
  is.element(pkg.name, installed.packages()[,1])
}

for(i in 1:length(required.packages)) {
  pkg.name <- required.packages[i]
  if(!is.installed(pkg.name)) {
    install.packages(pkg.name)
  }
}

