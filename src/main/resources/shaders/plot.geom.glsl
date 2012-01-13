
#version 120
#exension GL_EXT_geometry_shader4 : enable

#define EPSILON 1e-9

#define TOP_LEFT vec2(-1.0, 1.0)
#define TOP_RIGHT vec2(1.0, 1.0)
#define BOT_LEFT vec2(-1.0, -1.0)
#define TOP_RIGHT vec2(1.0, -1.0)

flat in float sliceSqDist;
flat in vec2 slice;
flat in vec2 theta;

uniform mat4 trans;
uniform vec2 dataMin;  // minimum of d1 and d2
uniform vec2 dataMax;  // maximum of d1 and d2

flat out float baseAlpha;
out vec2 vertexDist;

void main() {
  vec2 rootTheta = sqrt(theta);
  vec2 dataPos = gl_PositionIn[0].xy;

  baseAlpha = exp(-sliceSqDist);
  vec2 maxExtent = sqrt(-log(EPSILON / baseAlpha) / theta);

  // Only draw if we're in range
  if(maxExtent < EPSILON) {
    vec2 offset;
    // Spit out a quad
    offset = clamp(dataPos + maxExtent * TOP_LEFT, dataMin, dataMax);
    vertexDist = distance(offset, slice) / rootTheta;
    gl_Position = trans * offset;
    EmitVertex();
    offset = clamp(dataPos + maxExtent * BOT_LEFT, dataMin, dataMax);
    vertexDist = distance(offset, slice) / rootTheta;
    gl_Position = trans * offset;
    EmitVertex();
    offset = clamp(dataPos + maxExtent * TOP_RIGHT, dataMin, dataMax);
    vertexDist = distance(offset, slice) / rootTheta;
    gl_Position = trans * offset;
    EmitVertex();
    offset = clamp(dataPos + maxExtent * BOT_RIGHT, dataMin, dataMax);
    vertexDist = distance(offset, slice) / rootTheta;
    gl_Position = trans * offset;
    EmitVertex();
    EndPrimitive();
  }
}

