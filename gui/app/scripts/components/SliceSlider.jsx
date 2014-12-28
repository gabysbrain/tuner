/** @jsx React.DOM */

'use strict';

var React = require('react');
var PropTypes = React.PropTypes;

var Slider = require('./Slider.jsx');
var sliderStep = require('./utils').sliderStep;

var SliceSlider = React.createClass({
  propTypes: {
    name: PropTypes.string.isRequired,
    value: PropTypes.number.isRequired,
    lowValue: PropTypes.number.isRequired,
    highValue: PropTypes.number.isRequired,
    actions: PropTypes.object.isRequired,
    id: PropTypes.string
  },

  changeSlice: function(e, name, newVal) {
    this.props.actions.changeSlice(this.props.name, newVal, e);
  },

  render: function() {
    var step = sliderStep(this.props.highValue,
                          this.props.lowValue);
    return (
      <div className="slice-control">
        <label>
          {this.props.name} 
          <Slider
            id={this.props.id}
            value={this.props.value}
            min={this.props.lowValue}
            max={this.props.highValue}
            step={step}
            onChange={this.changeSlice} />
        </label>
      </div>
    );
  }

});

module.exports = SliceSlider;