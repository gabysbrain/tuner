/**
 * The entry point for the project viewer
 */

'use strict';

var fs = require('fs');
var React = require('react');
var Cortex = require('cortexjs');
var GuiActions = require('./actions/GuiActions');
var data = require('../../proj.json');

var cortex = new Cortex(data);
var actions = new GuiActions(cortex);
var ProjectViewer = require('./components/ProjectViewer.jsx');

function renderApp() {
  React.render(
    React.createElement(ProjectViewer, {actions: actions, project: data}),
    document.getElementById('app-frame')
  );
}

cortex.on('update', renderApp);

//window.onload(function() {
  renderApp(); // Initial render
//});
