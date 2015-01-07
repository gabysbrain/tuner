var gulp = require('gulp');
var browserify = require('browserify');
var source = require('vinyl-source-stream');
var config = require('../config');

var libs = [
  "color",
  "cortexjs",
  "gl-matrix",
  "jquery",
  "jquery-ui",
  "react"
];

gulp.task('js:vendor', function() {
  var b = browserify({debug: false});
  libs.forEach(function(lib) {
    b.require(lib); // need external requires so modules get exposed
  });
  return b.bundle()
    .pipe(source('vendor.js'))
    .pipe(gulp.dest(config.dest.scripts));
});

gulp.task('js:app', function() {
  var b = browserify('./' + config.src.scripts + '/app.js', {debug: true});
  b.external(libs)
   .transform('reactify')
   .transform('glslify');
  return b.bundle()
    .pipe(source('main.js'))
    .pipe(gulp.dest(config.dest.scripts));
});

gulp.task('js', ['js:app']);
