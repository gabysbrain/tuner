var gulp = require('gulp');

gulp.task('build', ['sass', 'js:vendor', 'js', 'html']);
