'use strict';

module.exports = {
  sliderStep: function(minVal, maxVal) {
    // 100 steps for now...
    return (maxVal-minVal) / 100;
  }
};