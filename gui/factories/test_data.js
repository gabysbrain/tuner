'use strict';

var Factory = require('rosie').Factory;

Factory.define('input')
  .sequence('name', function(i) {return 'x'+i;})
  .attr('minRange', 0)
  .attr('maxRange', 1);

Factory.define('output')
  .sequence('name', function(i) {return 'y'+i;})
  .attr('minimize', false);

Factory.define('slice')
  .sequence('name', function(i) {return 'x'+i;})
  .attr('value', 0.5);

Factory.define('zoom')
  .sequence('name', function(i) {return 'x'+i;})
  .attr('lowValue', 0.2)
  .attr('highValue', 0.8);

/**
 * Default has 4 inputs and 2 outputs
 */
Factory.define('viewableProject')
  .attr('name', 'test')
  .attr('inputs', ['inputs'], function(inputs) {
    if(!inputs) {inputs = [{}, {}, {}, {}];}
    return inputs.map(function(data) {
      return Factory.attributes('input', data);
    });
  })
  .attr('outputs', ['outputs'], function(outputs) {
    if(!outputs) {outputs = [{}, {}];}
    return outputs.map(function(data) {
      return Factory.attributes('output', data);
    });
  })
  .attr('currentVis', ['inputs', 'outputs'], function(inputs, outputs) {
    return {
      currentSlice: inputs.map(function(input) {
        return Factory.attributes('slice', {name: input.name});
      }),
      currentZoom: inputs.map(function(input) {
        return Factory.attributes('zoom', {name: input.name});
      }),
      response1: outputs[0].name,
      response2: outputs[1].name,
      currentVis: 'hyperslice',
      currentMetric: 'value'
    };
  });

  module.exports = Factory;