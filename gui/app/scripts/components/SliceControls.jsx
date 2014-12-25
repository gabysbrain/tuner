/** @jsx React.DOM */

var React = require('react');
var PropTypes = React.PropTypes;

var SliceSlider = require('./SliceSlider.jsx');

var SliceControls = React.createClass({
  propTypes: {
    project: PropTypes.object.isRequired,
    actions: PropTypes.object.isRequired
  },

  render: function() {
    var currentVis = this.props.project.currentVis;
    var actions = this.props.actions;
    var controls = currentVis.currentSlice.map(function(s, i) {
      return <SliceSlider name={s.name}
                          value={s.value}
                          lowValue={currentVis.currentZoom[i].lowValue}
                          highValue={currentVis.currentZoom[i].highValue}
                          actions={actions} />

    });
    return (
      <div>
        {controls}
      </div>
    );
  }

});

module.exports = SliceControls;