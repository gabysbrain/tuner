/** @jsx React.DOM */

'use strict';

var React = require('react');
var PropTypes = React.PropTypes;

var RangeSlider = require('./RangeSlider.jsx');
var sliderStep = require('./utils').sliderStep;

var ZoomSlider = React.createClass({
  propTypes: {
    name: PropTypes.string.isRequired,
    lowValue: PropTypes.number.isRequired,
    highValue: PropTypes.number.isRequired,
    paramMin: PropTypes.number.isRequired,
    paramMax: PropTypes.number.isRequired,
    actions: PropTypes.object.isRequired,
    id: PropTypes.string
  },

  changeZoom: function(e, newLow, newHigh) {
    actions.changeZoom(this.props.zoom, newLow, newHigh, e);
  },

  render: function() {
    var step = sliderStep(this.props.paramMin,
                          this.props.paramMax);
    return (
      <div className="zoom-control">
        <label>
          {this.props.name} 
          <RangeSlider
            id={this.props.id}
            lowValue={this.props.lowValue}
            highValue={this.props.highValue}
            min={this.props.paramMin}
            max={this.props.paramMax}
            step={step}
            onChange={this.changeZoom} />
        </label>
      </div>
    );
  }

});

module.exports = ZoomSlider;