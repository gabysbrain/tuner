package tuner.gui.util

import javax.media.opengl.{GL,GL2,GL2ES2}
import javax.media.opengl.GLAutoDrawable

/**
 * Special loader for the continuous plot stuff since 
 * the vertex shader gets dynamically created
 */
object GPPlotGlsl {
  def fromResource(gl:GL, numDims:Int, fragment:String) = {
    val vertSource = new GPPlotVertexShader(numDims).toString
    //println(vertSource)
    //val geomSource = Glsl.readResource(geom)
    val fragSource = Glsl.readResource(fragment)

    //new Glsl(drawable, vertSource, Some(geomSource), fragSource)
    new Glsl(gl, vertSource, None, fragSource, List(("geomOffset", 0)))
  }

  def numVec4(numDims:Int) = (numDims / 4.0).ceil.toInt
  def padCount(numDims:Int) = (4 - (numDims%4)) % 4

}


class GPPlotVertexShader(numDims:Int) {
  val template = """
  #version 120

  // Attributes
  %s
  //attribute float response;
  attribute vec2 geomOffset;

  // Uniforms
  uniform float maxSqDist;
  uniform mat4 trans;
  uniform int d1;
  uniform int d2;
  uniform vec2 dataMin; // minimum of d1 and d2
  uniform vec2 dataMax; // maximum of d1 and d2
  %s
  %s

  // Outputs
  varying float centerSqDist;
  varying vec2 vertexSqDist;
  varying vec2 theta;

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
    vec2 dataPos = vec2(getDimValue(d1), getDimValue(d2));
    vec2 slice = vec2(getSliceValue(d1), getSliceValue(d2));
    theta = vec2(getThetaValue(d1), getThetaValue(d2));

    // This won't get rasterized if the distance is too great
    vec2 actOffset = centerSqDist < maxSqDist ? dataMax * geomOffset : vec2(0.0, 0.0);
    //vec2 actOffset = dataMax * geomOffset;
    vec2 offset = clamp(dataPos + actOffset, dataMin, dataMax);
    vec2 vertDist = offset - dataPos;
    vertexSqDist = vertDist * vertDist;
    gl_Position = trans * vec4(offset, 0.0, 1.0);
  }
  """
  
  override def toString = 
    template.format(
      attribSrc(numDims), 
      sliceSrc(numDims),
      thetaSrc(numDims),
      getDimsFuncSrc(numDims, "getDimValue", "data"),
      getDimsFuncSrc(numDims, "getSliceValue", "slice"),
      getDimsFuncSrc(numDims, "getThetaValue", "theta"),
      ttlDistSrc(numDims))

  private def attribSrc(numDims:Int) = 
    (0 until GPPlotGlsl.numVec4(numDims)).map("attribute vec4 data" + _ + ";").
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
      "vec4 rawDist%d = data%d - slice%d;".format(dd, dd, dd) + "\n" +
      "vec4 sqDist%d = theta%d * rawDist%d * rawDist%d;".format(dd, dd, dd, dd)
    }.reduceLeft(_ + "\n" + _) + "\n" +
    "centerSqDist = " + 
    (0 until GPPlotGlsl.numVec4(numDims)).map {dd =>
      "sqDist%d.x + sqDist%d.y + sqDist%d.z + sqDist%d.w".format(dd, dd, dd, dd)
    }.reduceLeft(_ + " + " + _) + ";\n"

  /**
   * Generates the source code to separate the dimension values
   */
  private def getDimsFuncSrc(numDims:Int, func:String, varName:String) = 
    "float %s(int d) {\n".format(func) + 
    (0 until GPPlotGlsl.numVec4(numDims)).map {dd =>
      "if(d==%d) return %s%d.x;\n".format(dd*4+0, varName, dd) +
      "if(d==%d) return %s%d.y;\n".format(dd*4+1, varName, dd) +
      "if(d==%d) return %s%d.z;\n".format(dd*4+2, varName, dd) +
      "if(d==%d) return %s%d.w;\n".format(dd*4+3, varName, dd)
    }.reduceLeft(_ + "\n" + _) +
    "}\n"
}

