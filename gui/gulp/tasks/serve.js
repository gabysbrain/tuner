var gulp = require('gulp');
var config = require('../config');

gulp.task('serve', function() {
  var browserSync = require('browser-sync');
  return browserSync({
    server: {
      baseDir: config.dest.base
    },
    logConnections: true,
    browser: config.browser
  });
});