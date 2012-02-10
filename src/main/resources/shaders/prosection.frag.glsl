
#version 120

varying vec2 screenPos; // needed for drawing the circle
varying float fragValue;
varying float dimsPassFlag;

uniform float radius;

void main() {
  // Nothing made it past the filter...
  if(dimsPassFlag == 0.0)
    discard;
  
  if(distance(gl_FragCoord.xy, screenPos) > radius) 
    discard;
  
  gl_FragColor = vec4(fragValue, 0.0, 0.0, 1.0);
}

