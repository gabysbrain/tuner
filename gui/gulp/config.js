
var srcBase = 'app';
var destBase = 'build';

module.exports = {
  src: {
    base: srcBase,
    scss: srcBase + '/scss',
    scripts: srcBase + '/scripts'
  },
  dest: {
    base: destBase,
    css: destBase,
    scripts: destBase
  },
  bower: 'app/bower_components',
  browser: 'google chrome'
};