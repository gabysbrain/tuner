/** @jsx React.DOM */

var React = require('react');
var PropTypes = React.PropTypes;

var VisMethodControls = React.createClass({

  propTypes: {
    value: PropTypes.string.isRequired,
    actions: PropTypes.object.isRequired
  },

  changeMethod: function(e) {
    this.props.actions.changePSView(e.target.value);
  },

  render: function() {
    return (
      <ul className="controls">
        <li>
          <label>
            Hyperslice
            <input type="radio" id="hyperslice-vis" name="vis-method"
                   value="hyperslice"
                   checked={this.props.value==="hyperslice"}
                   onChange={this.changeMethod} />
          </label>
        </li>
        <li>
          <label>
            SPLOM
            <input type="radio" id="splom-vis" name="vis-method"
                   value="splom"
                   checked={this.props.value==="splom"}
                   onChange={this.changeMethod} />
          </label>
        </li>
      </ul>
    );
  }

});

module.exports = VisMethodControls;