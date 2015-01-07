var gulp = require('gulp');
var sass = require('gulp-ruby-sass');
var config = require('../config');

gulp.task('sass', function() {
  return gulp.src(config.src.scss + '/app.scss')
    .pipe(sass())
    .on('error', function(err) {console.error(err.message)})
    .pipe(gulp.dest(config.dest.css));
});