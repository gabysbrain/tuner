/** @jsx React.DOM */

var React = require('react');
var PropTypes = React.PropTypes;

var ResponseSelector = React.createClass({

  propTypes: {
    responseId: PropTypes.number.isRequired,
    responses: PropTypes.array.isRequired,
    value: PropTypes.string.isRequired,
    actions: PropTypes.object.isRequired,
    id: PropTypes.string
  },

  changeResponse: function(e) {
    this.props.actions.changeResponse(this.props.responseId, e.target.value, e);
  },

  render: function() {
    var resps = ["None"].concat(this.props.responses);
    var options = resps.map(function(response) {
      return(
        <option value={response} key={response}>
          {response}
        </option>
      );
    });
    return (
      <select id={this.props.id} onChange={this.changeResponse} 
              value={this.props.value}>
        {options}
      }
      </select>
    );
  }

});


module.exports = ResponseSelector;