
#version 120

varying float respValue;
varying float centerSqDist;
varying vec2 vertexDist;
varying vec2 theta;

//uniform float mean;
uniform float sig2;

void main() {
  vec2 vertexSqDist = vertexDist * vertexDist;
  vec2 weightedDist = theta * vertexSqDist;
  float ttlDist = centerSqDist + vertexSqDist.x + vertexSqDist.y;
  float alpha = sig2 * respValue * exp(-10.0*ttlDist) / 13.6;
  //alpha = 0.4;
  //alpha = 1.0;
  gl_FragColor = vec4(1.0, 0.0, 0.0, alpha);
}

