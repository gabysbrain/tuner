/** @jsx React.DOM */

var jsdom = require('mocha-jsdom');

var React = require('react/addons');

var SliceSlider, $, TestUtils;

describe('SliceSlider', function() {
  jsdom();

  before(function() {
    SliceSlider = require('../../app/scripts/components/SliceSlider.jsx');
    //$ = require('jquery');
    //require('jquery-ui');
    TestUtils = React.addons.TestUtils;
  });

  it('renders the correct parameter name', function() {

    var slider = TestUtils.renderIntoDocument(
      <SliceSlider inputName="x1" value={0.5} />
    );

    // Make sure the label has the correct value
    var label = TestUtils.findRenderedDOMComponentWithTag(slider, 'label');
    expect(label.getDOMNode().textContent).toEqual('x1');
  });
  
});