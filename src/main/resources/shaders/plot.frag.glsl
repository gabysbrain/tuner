
#version 120

flat in float baseAlpha;
in vec2 vertexDist;

out vec4 myColor;

void main() {
  float ttlDist = dot(vertexDist, vertexDist);
  //float alpha = baseAlpha * exp(-ttlDist);
  float alpha = 1.0;
  myColor = vec4(1.0, 0.0, 0.0, alpha);
}

