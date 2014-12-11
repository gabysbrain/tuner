/** @jsx React.DOM */

jest.dontMock('../Slider.jsx')
    .dontMock('jquery')
    .dontMock('jquery-ui');

var React, Slider, $, TestUtils;

describe('Slider', function() {
  beforeEach(function() {
    React = require('react/addons');
    Slider = require('../Slider.jsx');
    $ = require('jquery');
    require('jquery-ui');
    TestUtils = React.addons.TestUtils;
  });

  it('begins with the correct value', function() {
    var slider = TestUtils.renderIntoDocument(
      <Slider initialValue={0.5} />
    );

    // Make sure the initial value is correct
    expect($(slider.getDOMNode()).slider("value")).toEqual(0.5);
  });

  it('generates an event when changed', function() {
    var changeListener = jest.genMockFunction();

    var slider = TestUtils.renderIntoDocument(
      <Slider initialValue={0.5} onChange={changeListener} />
    );

    // Sanity check on the value
    expect($(slider.getDOMNode()).slider("value")).toEqual(0.5);

    // Make sure that we send an event when the value changes
    // FIXME: maybe sending drag events would be better...
    $(slider.getDOMNode()).slider("value", 0.7);

    expect(changeListener.mock.calls.length).toEqual(1);
    expect(changeListener.mock.calls[0][1]).toEqual(0.7);
  });
});