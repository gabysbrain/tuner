
#version 120

// inputs
attribute vec2 dataPt;
attribute float dataVal;

// Unforms
uniform mat4 trans;

// Outputs
varying float pixelVal;

void main() {
  pixelVal = dataVal;

  // Convert the data coords into window coords
  gl_Position = trans * vec4(dataPt, 0.0, 1.0);
}

