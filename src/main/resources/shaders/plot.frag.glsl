
#version 120

varying float centerSqDist;
varying vec2 vertexSqDist;
varying vec2 theta;

void main() {
  vec2 weightedDist = theta * vertexSqDist;
  float ttlDist = centerSqDist + vertexSqDist.x + vertexSqDist.y;
  //float alpha = exp(-ttlDist);
  float alpha = 0.8;
  gl_FragColor = vec4(1.0, 0.0, 0.0, alpha);
}

