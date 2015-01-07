var gulp = require('gulp');
var config = require('../config');

gulp.task('watch', ['build'], function() {

  var reload = require('browser-sync').reload;

  // watch .scss files
  gulp.watch(config.src.scss + '/**/*.scss', ['scss']);

  // watch js files
  gulp.watch(config.src.scripts + '/**/*', ['js', reload]);

  // watch html files
  gulp.watch(config.src.base + '/**/*.html', ['html', reload]);
});