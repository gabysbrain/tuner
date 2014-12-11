/** @jsx React.DOM */

'use strict';

var React = require('react');
var PropTypes = React.PropTypes;
var $ = require('jquery');

require('jquery-ui');

var Slider = React.createClass({
  propTypes: {
    min: PropTypes.number,
    max: PropTypes.number,
    step: PropTypes.number,
    value: PropTypes.number.isRequired,
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
    $(this.getDOMNode()).slider(this.props);
    $(this.getDOMNode()).slider("value", this.props.value);

    // Only need to bind the event handler here
    $(this.getDOMNode()).on("change", this._onChange);
    $(this.getDOMNode()).on("slide", this._onChange);
  },

  componentDidUpdate: function(prevProps, prevState) {
    $(this.getDOMNode()).slider(this.props);
    $(this.getDOMNode()).slider("value", this.props.value);
  },

  render: function() {
    return (
      <div />
    );
  },

  _onChange: function(event, ui) {
    console.log('here');
    var prevVal = this.props.value;
    if(prevVal !== ui.value && this.props.onChange) {
      this.props.onChange(event, ui.value);
    }
  }

});

module.exports = Slider;