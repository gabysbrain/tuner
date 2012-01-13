package tuner.gui.util

import javax.media.opengl.GL
import javax.media.opengl.GL2

/**
 * Special loader for the continuous plot stuff since 
 * the vertex shader gets dynamically created
 */
object GPPlotGlsl {
  def fromResource(gl:GL2, numDims:Int, geom:String, fragment:String) = {
    val vertSource = new GPPlotVertexShader(numDims).toString
    val geomSource = Glsl.readResource(geom)
    val fragSource = Glsl.readResource(fragment)

    new Glsl(gl, vertSource, Some(geomSource), fragSource)
  }

  def numVec4(numDims:Int) = (numDims / 4.0).ceil.toInt

}


class GPPlotVertexShader(numDims:Int) {
  val template = """
  #version 120
  
  // Attributes
  %s

  // Uniforms
  uniform int d1;
  uniform int d2;
  %s
  %s

  // Outputs
  flat out float sliceSqDist;
  flat out vec2 slice;
  flat out vec2 theta;

  // Function to get data values
  %s

  // Function to get slice values
  %s

  // Function to get theta values
  %s

  void main() {
    // Compute the squared distance
    %s

    // Assign all the projected stuff
    gl_Position = vec4(getDimValue(d1), getDimValue(d2), 0.0, 1.0);
    slice = vec2(getSliceValue(d1), getSliceValue(d2));
    theta = vec2(getThetaValue(d1), getThetaValue(d2));
  }
  """
  
  override def toString = 
    template.format(
      attribSrc(numDims), 
      sliceSrc(numDims),
      thetaSrc(numDims),
      getDimsFuncSrc(numDims, "getDimValue", "d"),
      getDimsFuncSrc(numDims, "getSliceValue", "slice"),
      getDimsFuncSrc(numDims, "getThetaValue", "theta"),
      ttlDistSrc(numDims))

  private def attribSrc(numDims:Int) = 
    (0 until GPPlotGlsl.numVec4(numDims)).map("in vec4 p" + _ + ";").
      reduceLeft(_ + "\n" + _)
  private def sliceSrc(numDims:Int) =
    (0 until GPPlotGlsl.numVec4(numDims)).map("uniform vec4 slice" + _ + ";").
      reduceLeft(_ + "\n" + _)
  private def thetaSrc(numDims:Int) =
    (0 until GPPlotGlsl.numVec4(numDims)).map("uniform vec4 theta" + _ + ";").
      reduceLeft(_ + "\n" + _)

  /**
   * code to compute the distance between a data point and the slice point
   */
  private def ttlDistSrc(numDims:Int) =
    (0 until GPPlotGlsl.numVec4(numDims)).map {dd =>
      "vec4 dist%d = theta%d * (p%d - slice%d);".format(dd, dd, dd, dd)
    }.reduceLeft(_ + "\n" + _) +
    "sliceSqDist = " + 
    (0 until GPPlotGlsl.numVec4(numDims)).map {dd =>
      "dot(dist%d, dist%d)".format(dd, dd)
    }.reduceLeft(_ + " + " + _) + ";\n"

  /**
   * Generates the source code to separate the dimension values
   */
  private def getDimsFuncSrc(numDims:Int, func:String, varName:String) = 
    "float %s(int d) {\n".format(func) + 
    "  switch(d) {\n" +
    (0 until GPPlotGlsl.numVec4(numDims)).map {dd =>
      "case %d: return %s%d.x; break;\n".format(dd*4+0, varName, dd) +
      "case %d: return %s%d.y; break;\n".format(dd*4+1, varName, dd) +
      "case %d: return %s%d.z; break;\n".format(dd*4+2, varName, dd) +
      "case %d: return %s%d.w; break;\n".format(dd*4+3, varName, dd)
    }.reduceLeft(_ + "\n" + _) +
    "  }\n" +
    "}\n"
}

