'use strict';

var mtx = require('gl-matrix');

function compileShader(gl, vertexShaderSource, fragmentShaderSource) {
  var vertexShader = gl.createShader(gl.VERTEX_SHADER);
  gl.shaderSource(vertexShader, vertexShaderSource);
  gl.compileShader(vertexShader); 

  var fragmentShader = gl.createShader(gl.FRAGMENT_SHADER);
  gl.shaderSource(fragmentShader, fragmentShaderSource);
  gl.compileShader(fragmentShader);

  //errors in shaders?
  if (!gl.getShaderParameter(vertexShader, gl.COMPILE_STATUS)) {
    alert("vertexShaderInfoLog:\n" + gl.getShaderInfoLog(vertexShader));
  } 
  if (!gl.getShaderParameter(fragmentShader, gl.COMPILE_STATUS)) {
    alert("fragmentShaderInfoLog:\n" + gl.getShaderInfoLog(fragmentShader));
  }     

  var program = gl.createProgram();
  gl.attachShader(program, vertexShader);
  gl.attachShader(program, fragmentShader);
  gl.linkProgram(program);

  if (!gl.getProgramParameter(program, gl.LINK_STATUS)) {
    alert("Could not initialise shaders.");
    console.log("ProgramInfoLog:\n" + gl.getProgramInfoLog(program));   
  }   

  return program;
}

function mapVal(val, min1, max1, min2, max2) {
  var pct = (val-min1)/(max1-min1);
  return min2 + (max2-min2) * pct;
}

function hypersliceLayout(numDims, width, height, spacing) {
  // make things square
  var squareSize = Math.min(width, height);
  var totalSpacing = spacing * (numDims-1);

  var remWidth = squareSize - totalSpacing;
  var remHeight = squareSize - totalSpacing;
  var panelWidth = remWidth / numDims;
  var panelHeight = remHeight / numDims;

  // the base plots are 0-1 scaled
  var scaleMtx = mtx.mat4.create();
  mtx.mat4.scale(scaleMtx, scaleMtx, [panelWidth, panelHeight, 1]);

  var transMtxs = [];
  // each pair of dimensions gets a transformation matrix
  for(var xDim=0; xDim<numDims; xDim++) {
    for(var yDim=0; yDim<numDims; yDim++) {
      if(xDim !== yDim) {
        var xMove = xDim * (panelWidth + spacing);
        var yMove = yDim * (panelWidth + spacing);
        var transMtx = mtx.mat4.create();
        mtx.mat4.translate(transMtx, transMtx, [xMove, yMove, 0]);

        var finalMtx = mtx.mat4.create();
        mtx.mat4.mul(finalMtx, transMtx, scaleMtx);
        transMtxs.push({
          xDim: xDim,
          yDim: yDim,
          transMtx: finalMtx
        });
      }
    }
  }

  return transMtxs;
}

module.exports = {
  compileShader: compileShader,
  mapVal: mapVal,
  hypersliceLayout: hypersliceLayout
}

