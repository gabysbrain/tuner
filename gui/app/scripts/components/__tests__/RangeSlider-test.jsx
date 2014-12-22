/** @jsx React.DOM */

require('./react_helper');

var RangeSlider, $;

describe('RangeSlider', function() {

  beforeEach(function() {
    RangeSlider = require('../RangeSlider.jsx');
    $ = require('jquery');
    require('jquery-ui');
  });

  it('begins with the correct value', function() {
    var slider = TestUtils.renderIntoDocument(
      <RangeSlider lowValue={0.5} highValue={0.7} />
    );

    // Make sure the initial value is correct
    expect($(slider.getDOMNode()).slider("values")[0]).to.equal(0.5);
    expect($(slider.getDOMNode()).slider("values")[1]).to.equal(0.7);
  });

  it('runs the proper javascript setup code', function() {
    var slider = TestUtils.renderIntoDocument(
      <RangeSlider lowValue={0.5} highValue={0.7} />
    );

    expect($(slider.getDOMNode()).hasClass('ui-slider')).to.be.true;
  });

  it('properly updates the value', function() {
    var slider = TestUtils.renderIntoDocument(
      <RangeSlider lowValue={0.5} highValue={0.7} />
    );

    // Sanity check on the value
    expect($(slider.getDOMNode()).slider("values")[0]).to.equal(0.5);
    expect($(slider.getDOMNode()).slider("values")[1]).to.equal(0.7);

    slider.setProps({lowValue: 0.7, highValue: 0.75})
    expect($(slider.getDOMNode()).slider("values")[0]).to.equal(0.7);
    expect($(slider.getDOMNode()).slider("values")[1]).to.equal(0.75);
  });

  xit('generates an event when changed', function() {
    var changeListener = sinon.spy();

    var slider = TestUtils.renderIntoDocument(
      <RangeSlider lowValue={0.5} highValue={0.7} onChange={changeListener} />
    );

    // Sanity check on the value
    expect($(slider.getDOMNode()).slider("values")).to.equal(0.5);

    // Make sure that we send an event when the value changes
    // FIXME: sending drag events would be better...
    var dragHandle = $(slider.getDOMNode()).find('.ui-slider-handle').first();
    // TestUtils.Simulate.mouseDown(dragHandle);
    // TestUtils.Simulate.mouseMove(dragHandle);
    // TestUtils.Simulate.mouseUp(dragHandle);
    //TestUtils.Simulate.drag(dragHandle, {dx: 10, dy: 10});
    $(dragHandle).trigger('drag', {dx: 10, dy: 10});

    expect(changeListener.called).to.be.true;
    expect(changeListener.getCall(0).args[1]).to.equal([0.6, 0.7]);
  });
});