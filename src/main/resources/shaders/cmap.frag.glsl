
#version 120

varying vec2 texCoord;

uniform sampler2D values;
uniform float filterLevel;

uniform float maxVal;
uniform vec3 minColor;
uniform vec3 maxColor;
uniform vec3 filterColor;

void main() {
  vec4 val = texture2D(values, texCoord);
  if(val.r < filterLevel) {
    gl_FragColor = vec4(filterColor, 1.0);
  } else {
    float pct = (val.r - filterLevel) / (maxVal - filterLevel);
    gl_FragColor = vec4(mix(minColor, maxColor, pct), 1.0);
  }
  //gl_FragColor = val;
}

