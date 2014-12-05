'use strict';

var config = require('../config');
var gulp = require('gulp');
var prefix = require('gulp-autoprefixer');
var csso = require('gulp-csso');
var fingerprint = require('gulp-fingerprint');
var sass = require('gulp-ruby-sass');
var size = require('gulp-size');

// Styles
gulp.task('styles', function () {
  // See https://github.com/andrew/node-sass for more options
  return gulp.src('app/scss/app.scss')
    .pipe(sass({
      style: 'nested',
      loadPath: [config.bower + '/foundation-apps/scss']
    }))
    .pipe(prefix({
      browsers: ['last 2 versions', 'ie 10']
    }))
    .pipe(gulp.dest(config.dist + '/styles'))
    .pipe(size());
});

// Styles Dist
gulp.task('styles:dist', function () {
  var manifest = require('../../dist/image-manifest');
  // See https://github.com/andrew/node-sass for more options
  return gulp.src('app/scss/app.scss')
    .pipe(sass({
      includePaths: [config.bower + '/foundation/scss']
    }))
    // .pipe(prefix('last 1 version'))  // add vendor prefixes if necessary
    .pipe(fingerprint(manifest, {verbose: true}))
    .pipe(csso())  // minify css
    .pipe(gulp.dest(config.dist + '/styles'))
    .pipe(size());
});
