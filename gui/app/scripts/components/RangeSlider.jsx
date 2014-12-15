/** @jsx React.DOM */

'use strict';

var React = require('react');
var PropTypes = React.PropTypes;

var $ = require('jquery');
require('jquery-ui');

var RangeSlider = React.createClass({
  propTypes: {
    min: PropTypes.number,
    max: PropTypes.number,
    step: PropTypes.number,
    lowValue: PropTypes.number.isRequired,
    highValue: PropTypes.number.isRequired,
    onChange: PropTypes.func
  },

  getDefaultProps: function() {
    return {
      min: 0,
      max: 1,
      step: 0.01
    }
  },

  componentDidMount: function() {
    $(this.getDOMNode()).slider({
      min: this.props.min,
      max: this.props.max,
      step: this.props.step,
      values: [this.props.lowValue, this.props.highValue]
    });

    $(this.getDOMNode()).on("change", this._onChange);
    $(this.getDOMNode()).on("slide", this._onChange);
  },

  componentDidUpdate: function(prevProps, prevState) {
    $(this.getDOMNode()).slider({
      min: this.props.min,
      max: this.props.max,
      step: this.props.step,
      values: [this.props.lowValue, this.props.highValue]
    });

    $(this.getDOMNode()).on("change", this._onChange);
    $(this.getDOMNode()).on("slide", this._onChange);
  },

  render: function() {
    return (
      <div />
    );
  },

  _onChange: function(event, ui) {
    console.log('here');
    var prevLow = this.props.lowValue
    var prevHigh = this.props.highValue;
    if((prevLow !== ui.values[0] || prevHigh !== ui.values[1]) && 
        this.props.onChange) {
      this.props.onChange(event, ui.values[0], ui.values[1]);
    }
  }

});

module.exports = RangeSlider;