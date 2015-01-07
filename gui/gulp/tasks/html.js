var gulp = require('gulp');
var config = require('../config');

gulp.task('html', function() {
  return gulp.src(config.src.base + '/index.html')
    .pipe(gulp.dest(config.dest.base));
})