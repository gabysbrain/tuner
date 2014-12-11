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
    initialValue: PropTypes.number,
    onChange: PropTypes.func
  },

  getDefaultProps: function() {
    return {
      min: 0,
      max: 1,
      step: 0.01,
      initialValue: 0
    }
  },

  getInitialState: function() {
    return {
      value: this.props.initialValue
    };
  },

  componentDidMount: function() {
    $(this.getDOMNode()).slider(this.props);
    $(this.getDOMNode()).slider("value", this.state.value);

    // Only need to bind the event handler here
    $(this.getDOMNode()).on("slidechange", this._onChange);
  },

  componentDidUpdate: function(prevProps, prevState) {
    $(this.getDOMNode()).slider(this.props);
    $(this.getDOMNode()).slider("value", this.state.value);
  },

  render: function() {
    return (
      <div />
    );
  },

  _onChange: function(event, ui) {
    var prevVal = this.state.value;
    if(prevVal !== ui.value) {
      this.setState({value: ui.value});
      if(this.props.onChange) {
        this.props.onChange(event, this.state.value);
      }
    }
  }

});

module.exports = Slider;