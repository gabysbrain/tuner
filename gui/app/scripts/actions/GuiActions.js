'use strict';

/**
 * All possible actions for the interface
 */

var Actions = function(proj) {
  return {
    changeSlice: function(paramName, newVal, e) {
      var slice = proj.currentVis.currentSlice.find(function(elt) {
        return elt.name === paramName;
      });
      slice.value.set(newVal);
    },

    changeZoom: function(paramName, newLow, newHigh, e) {
      var zoom = proj.currentVis.currentZoom.find(function(elt) {
        return elt.name === paramName;
      });
      zoom.lowValue.set(newLow);
      zoom.highValue.set(newHigh);
    },

    changeResponse: function(response, newValue, e) {
      var respName = 'response' + response;
      proj.currentVis[respName].set(newValue);
    },

    changePSMetric: function(newValue, e) {
      proj.currentVis.currentMetric.set(newValue);
    },

    changePSView: function(newValue, e) {
      proj.currentVis.currentVis.set(newValue);
    }
  };
};

module.exports = Actions;