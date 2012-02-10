
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
  vec2 tmp = texture2D(values, texCoord).ra;
  float val = clamp(tmp.r, minVal, maxVal);
  //val = sqrt(val);
  if(tmp.y > 0.0) {
    if(invert) {
      float pct = (val - filterLevel) / (minVal - filterLevel);
      gl_FragColor = val > filterLevel ? filterColor : mix(minColor, maxColor, pct);
    } else {
      float pct = (val - filterLevel) / (maxVal - filterLevel);
      gl_FragColor = val < filterLevel ? filterColor : mix(minColor, maxColor, pct);
    }
  } else {
    gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
  }
}

