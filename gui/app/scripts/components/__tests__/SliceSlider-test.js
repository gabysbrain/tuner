/** @jsx React.DOM */

jest.dontMock('../SliceSlider.jsx')
    .dontMock('jquery')
    .dontMock('jquery-ui');

var React, SliceSlider, $, TestUtils;

describe('SliceSlider', function() {
  beforeEach(function() {
    React = require('react/addons');
    SliceSlider = require('../SliceSlider.jsx');
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