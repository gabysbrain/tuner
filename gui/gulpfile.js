var gulp = require('gulp');
var requireDir = require('require-dir');

requireDir('./gulp/tasks');

gulp.task('default', ['build'], function() {
  gulp.run('serve');
  gulp.start('watch');
})