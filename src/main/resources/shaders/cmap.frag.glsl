
#version 120

varying vec2 texCoord;

uniform sampler2D values;

void main() {
  gl_FragColor = texture2D(values, texCoord);
}

