
global.React = require( 'react/addons' );
global.TestUtils = React.addons.TestUtils;

before(function() {
  var jsdom = require("jsdom");

  global.document = jsdom.jsdom('<html><body></body></html>');
  global.window = document.parentWindow;
  global.navigator = window.navigator;
});
