
#version 120

varying vec2 pos;

uniform float sig2;
uniform sampler2D points;
uniform sampler2D invCorMtx;
uniform vec4 theta;
uniform vec4 focusPoint;
uniform int numPoints;

void main() {
  float ttlErr = 0.0;
  vec4 x;
  for(int i=0; i<numPoints; i++) {
    x = sample(points, i, 1-4);
    vec4 dist = theta * (x - focusPoint) * (x-focusPoint);
    float r = exp(sum(dist));
  }
  gl_FragColor = vec4(0.7, 0.0, 0.0, 1.0);
}

