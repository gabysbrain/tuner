/** @jsx React.DOM */

require('./react_helper');

var ZoomSlider;

describe('ZoomSlider', function() {
  beforeEach(function() {
    ZoomSlider = require('../ZoomSlider.jsx');
  });

  it('renders the correct parameter name', function() {

    var slider = TestUtils.renderIntoDocument(
      <ZoomSlider name={'x1'} lowValue={0.4} highValue={0.6} 
                              paramMin={0.0} paramMax={1.0} />
    );

    // Make sure the label has the correct value
    var label = TestUtils.findRenderedDOMComponentWithTag(slider, 'label');
    expect(label.getDOMNode().textContent).to.equal('x1');
  });
  
});