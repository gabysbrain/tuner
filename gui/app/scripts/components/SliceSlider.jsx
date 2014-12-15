/** @jsx React.DOM */

'use strict';

var React = require('react');
var PropTypes = React.PropTypes;

var Slider = require('./Slider.jsx');

var SliceSlider = React.createClass({
  propTypes: {
    inputName: PropTypes.string.isRequired,
    value: PropTypes.number.isRequired,
    id: PropTypes.string,
    onChange: PropTypes.func
  },

  render: function() {
    return (
      <div className="sliceControl">
        <label for={this.props.id}>
          {this.props.inputName} 
        </label>
        <Slider
          id={this.props.id}
          value={this.props.value}
          onChange={this.props.onChange} />
      </div>
    );
  }

});

module.exports = SliceSlider;