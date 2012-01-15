
#version 120

varying float baseAlpha;
varying vec2 vertexDist;

void main() {
  float ttlDist = dot(vertexDist, vertexDist);
  //float alpha = baseAlpha * exp(-ttlDist);
  float alpha = 1.0;
  gl_FragColor = vec4(1.0, 0.0, 0.0, alpha);
}

