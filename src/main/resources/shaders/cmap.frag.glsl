
#version 120

varying vec2 texCoord;

uniform sampler2D values;
uniform float filterLevel;
uniform bool invert;

uniform float minVal;
uniform float maxVal;
uniform vec4 minColor;
uniform vec4 maxColor;
uniform vec4 filterColor;

void main() {
  float val = clamp(texture2D(values, texCoord), minVal, maxVal).r;
  val = sqrt(val);
  if(invert) {
    float pct = (val - filterLevel) / (minVal - filterLevel);
    gl_FragColor = val > filterLevel ? filterColor : mix(minColor, maxColor, pct);
  } else {
    float pct = (val - filterLevel) / (maxVal - filterLevel);
    gl_FragColor = val < filterLevel ? filterColor : mix(minColor, maxColor, pct);
  }
}

