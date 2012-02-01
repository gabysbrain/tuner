
#version 120

// Also doubles as the texture coordinate
attribute vec4 vPos;

uniform mat4 trans;

varying vec2 texCoord;

void main() {
  gl_Position = trans * vPos;
  texCoord = vPos.xy;
}

