/** @jsx React.DOM */

var React = require('react');
PropTypes = React.PropTypes;

var MetricViewControls = React.createClass({

  propTypes: {
    value: PropTypes.string.isRequired,
    actions: PropTypes.object.isRequired
  },

  changeMetric: function(e) {
    this.props.actions.changePSMetric(e.target.value);
  },

  render: function() {
    if(this.props.value !== "value" &&
       this.props.value !== "error" &&
       this.props.value !== "gain") {
      throw "'" + this.props.value + "' is not an acceptable metric"
    }

    return (
      <ul className="controls">
        <li>
          <label>
            Value
            <input type="radio" id="value-view" name="metric" 
                   value="value"
                   checked={this.props.value==="value"} 
                   onChange={this.changeMetric} />
          </label>
        </li>
        <li>
          <label>
            Error
            <input type="radio" id="error-view" name="metric" 
                   value="error"
                   checked={this.props.value==="error"} 
                   onChange={this.changeMetric} />
          </label>
        </li>
        <li>
          <label>
            Gain
            <input type="radio" id="gain-view" name="metric" 
                   value="gain"
                   checked={this.props.value==="gain"} 
                   onChange={this.changeMetric} />
          </label>
        </li>
      </ul>
    );
  }

});

module.exports = MetricViewControls;