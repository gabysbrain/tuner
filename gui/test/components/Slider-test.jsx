/** @jsx React.DOM */

var jsdom = require('mocha-jsdom');
var expect = require('chai').expect;

var React, Slider, $, TestUtils;

describe('Slider', function() {
  jsdom();

  beforeEach(function() {
    for (var i in require.cache) delete require.cache[i];
    React = require('react/addons');
    TestUtils = React.addons.TestUtils;
    Slider = require('../../app/scripts/components/Slider.jsx');
    $ = require('jquery');
    require('jquery-ui');
  });

  it('begins with the correct value', function() {
    var slider = TestUtils.renderIntoDocument(
      <Slider value={0.5} />
    );

    // Make sure the initial value is correct
    expect($(slider.getDOMNode()).slider("value")).to.equal(0.5);
  });

  it('runs the proper javascript setup code', function() {
    var slider = TestUtils.renderIntoDocument(
      <Slider value={0.5} />
    );

    expect($(slider.getDOMNode()).hasClass('ui-slider')).to.be.true();
  });

  it('properly updates the value', function() {
    var slider = TestUtils.renderIntoDocument(
      <Slider value={0.5} />
    );

    // Sanity check on the value
    expect($(slider.getDOMNode()).slider("value")).to.equal(0.5);

    slider.setProps({value: 0.7});
    expect($(slider.getDOMNode()).slider("value")).to.equal(0.7);
  });

  xit('generates an event when changed', function() {
    var changeListener = jest.genMockFunction();

    var slider = TestUtils.renderIntoDocument(
      <Slider value={0.5} onChange={changeListener} />
    );

    // Sanity check on the value
    expect($(slider.getDOMNode()).slider("value")).to.equal(0.5);

    // Make sure that we send an event when the value changes
    // FIXME: sending drag events would be better...
    var dragHandle = $(slider.getDOMNode()).find('.ui-slider-handle').first();
    // TestUtils.Simulate.mouseDown(dragHandle);
    // TestUtils.Simulate.mouseMove(dragHandle);
    // TestUtils.Simulate.mouseUp(dragHandle);
    //TestUtils.Simulate.drag(dragHandle, {dx: 10, dy: 10});
    $(dragHandle).trigger('drag', {dx: 10, dy: 10});

    expect(changeListener.mock.calls.length).to.equal(1);
    expect(changeListener.mock.calls[0][1]).to.equal(0.7);
  });
});