/** @jsx React.DOM */

var React = require('react');
var PropTypes = React.PropTypes;

var ZoomSlider = require('./ZoomSlider.jsx');

var ZoomControls = React.createClass({
  propTypes: {
    project: PropTypes.object.isRequired,
    actions: PropTypes.object.isRequired
  },

  render: function() {
    var actions = this.props.actions;
    var project = this.props.project;
    var controls = this.props.project.currentVis.currentZoom.map(function(z, i) {
      return <ZoomSlider name={z.name}
                         lowValue={z.lowValue}
                         highValue={z.highValue}
                         paramMin={project.inputs[i].minRange}
                         paramMax={project.inputs[i].maxRange} 
                         actions={actions} />

    });
    return (
      <div>
        {controls}
      </div>
    );
  }

});

module.exports = ZoomControls;