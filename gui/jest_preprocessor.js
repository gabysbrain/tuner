
/**
 * preprocessor for jest/JSX scripts
 */

var ReactTools = require('react-tools');
module.exports = {
  process: function(src) {
    return ReactTools.transform(src);
  }
}