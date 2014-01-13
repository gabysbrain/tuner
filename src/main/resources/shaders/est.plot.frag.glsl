
#version 120

varying float fragCoeff;
varying float centerSqDist;
varying vec2 vertexDist; // This needs to be the post-clamped distance
varying vec2 theta;

uniform float sig2;

void main() {
  vec2 vertexSqDist = vertexDist * vertexDist;
  vec2 weightedDist = theta * vertexSqDist;
  float ttlDist = centerSqDist + weightedDist.x + weightedDist.y;
  float redness = sig2 * fragCoeff * exp(-ttlDist);
  //alpha = 0.4;
  //alpha = 1.0;
  gl_FragColor = vec4(redness, 0.0, 0.0, 1.0);
}

