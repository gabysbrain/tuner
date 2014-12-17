/** @jsx React.DOM */

'use strict';

var React = require('react');
var PropTypes = React.PropTypes;

var Slider = require('./Slider.jsx');
var sliderStep = require('./utils').sliderStep;

var SliceSlider = React.createClass({
  propTypes: {
    slice: PropTypes.shape({
      name: PropTypes.string.isRequired,
      value: PropTypes.number.isRequired
    }).isRequired,
    zoom: PropTypes.shape({
      //name: PropTypes.string.isRequired,
      lowValue: PropTypes.number.isRequired,
      highValue: PropTypes.number.isRequired
    }).isRequired,
    id: PropTypes.string
  },

  changeSlice: function(e, name, newVal) {
    Actions.changeSlice(this.props.slice.name, newVal, e);
  },

  render: function() {
    var step = sliderStep(this.props.zoom.highValue,
                          this.props.zoom.lowValue);
    return (
      <div className="slice-control">
        <label for={this.props.id}>
          {this.props.slice.name} 
        </label>
        <Slider
          id={this.props.id}
          value={this.props.slice.value}
          min={this.props.zoom.lowValue}
          max={this.props.zoom.highValue}
          step={step}
          onChange={this.changeSlice} />
      </div>
    );
  }

});

module.exports = SliceSlider;