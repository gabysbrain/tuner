
#version 120

// inputs
varying float pixelVal;

void main() {
  gl_FragColor = vec4(pixelVal, 0.0, 0.0, 1.0);
}

