/** @jsx React.DOM */

var SliceSlider;

describe('SliceSlider', function() {
  beforeEach(function() {
    React = require('react/addons');
    SliceSlider = require('../SliceSlider.jsx');
    TestUtils = React.addons.TestUtils;
  });

  it('renders the correct parameter name', function() {

    var slider = TestUtils.renderIntoDocument(
      <SliceSlider name={'x1'} value={0.5} lowValue={0.0} highValue={1.0} />
    );

    // Make sure the label has the correct value
    var label = TestUtils.findRenderedDOMComponentWithTag(slider, 'label');
    expect(label.getDOMNode().textContent).to.equal('x1');
  });
  
});