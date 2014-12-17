/** @jsx React.DOM */

jest.dontMock('../ZoomSlider.jsx')
    .dontMock('jquery')
    .dontMock('jquery-ui');

var Cortex, React, ZoomSlider, TestUtils;

describe('ZoomSlider', function() {
  beforeEach(function() {
    React = require('react/addons');
    ZoomSlider = require('../ZoomSlider.jsx');
    TestUtils = React.addons.TestUtils;
  });

  it('renders the correct parameter name', function() {

    var slider = TestUtils.renderIntoDocument(
      <ZoomSlider name={'x1'} lowValue={0.4} highValue={0.6} 
                              paramMin={0.0} paramMax={1.0} />
    );

    // Make sure the label has the correct value
    var label = TestUtils.findRenderedDOMComponentWithTag(slider, 'label');
    expect(label.getDOMNode().textContent).toEqual('x1');
  });
  
});