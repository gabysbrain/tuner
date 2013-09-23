package tuner.gui.opengl

import org.jblas.DoubleMatrix

import javax.media.opengl.{GL,GL2,GL2ES2,GL2GL3}
import javax.media.opengl.GLAutoDrawable

import tuner.Config
import tuner.gui.util.Matrix4

/**
 * Special loader for the continuous plot stuff since 
 * the vertex shader gets dynamically created
 */
object Prosection {
  def fromResource(
      gl:GL2, numDims:Int, points:DoubleMatrix, values:DoubleMatrix) = 
    new Prosection(gl, numDims, points, values)

  def numVec4(numDims:Int) = (numDims / 4.0).ceil.toInt
  def padCount(numDims:Int) = (4 - (numDims%4)) % 4

}

class Prosection(gl:GL2, numDims:Int, 
                         points:DoubleMatrix,
                         values:DoubleMatrix)
    extends Glsl(gl, new ProsectionVertexShader(numDims).toString,
                     None, Glsl.readResource("/shaders/prosection.frag.glsl"), 
                     List()) {
  
  // Setup the display list
  val drawListId = gl.glGenLists(1)
  gl.glNewList(drawListId, GL2.GL_COMPILE)
    gl.glClearColor(0f, 0f, 0f, 0f)
    gl.glClear(GL.GL_COLOR_BUFFER_BIT)
    gl.glPointSize(10)
    gl.glBegin(GL.GL_POINTS)
    for(r <- 0 until points.rows) {
      val pt = points.getRow(r).toArray
      // Draw all the point data
      for(i <- 0 until Prosection.numVec4(numDims)) {
        val ptId = attribId("data" + i)
        val fieldVals = pt ++ Array(0.0, 0.0, 0.0, 0.0)
        
        gl.glVertexAttrib4f(ptId, fieldVals(i*4+0).toFloat, 
                                  fieldVals(i*4+1).toFloat, 
                                  fieldVals(i*4+2).toFloat, 
                                  fieldVals(i*4+3).toFloat)
      }
      val respId = attribId("value")
      gl.glVertexAttrib1f(respId, values.get(r).toFloat)

    }
    gl.glEnd
  gl.glEndList
  
  def draw(gl:GL2, textureId:Int, texWidth:Int, texHeight:Int,
                   trans:Matrix4,
                   xRange:(String,(Float,Float)), yRange:(String,(Float,Float)),
                   xDim:Int, yDim:Int,
                   minDimValues:Array[Float],
                   maxDimValues:Array[Float]) = {

    val (xr, yr) = if(xRange._1 < yRange._1) {
      (xRange, yRange)
    } else {
      (yRange, xRange)
    }
    
    // Enable shader program
    gl.glUseProgram(programId)

    // Save the viewport size before we kill it
    val vp = Array(0, 0, 0, 0)
    gl.glGetIntegerv(GL.GL_VIEWPORT, vp, 0)

    // Draw to a texture
    enableTextureTarget(gl, textureId, texWidth, texHeight)

    // Send down the uniforms for this set
    gl.glUniform1f(uniformId("radius"), 3.5f)
    gl.glUniformMatrix4fv(uniformId("trans"), 
                          1, false, trans.toArray, 0)
    gl.glUniform1i(uniformId("d1"), xDim)
    gl.glUniform1i(uniformId("d2"), yDim)
    gl.glUniform2f(uniformId("halfWindowSize"), texWidth.toFloat / 2, 
                                                texHeight.toFloat / 2)

    // Send down all the min and max values
    val minValArray = minDimValues ++ Array(0f, 0f, 0f, 0f) // These must pass
    val maxValArray = maxDimValues ++ Array(1f, 1f, 1f, 1f)
    for(i <- 0 until Prosection.numVec4(numDims)) {
      val minId = uniformId("minVal" + i)
      val maxId = uniformId("maxVal" + i)
      gl.glUniform4f(minId, minValArray(i*4+0),
                            minValArray(i*4+1),
                            minValArray(i*4+2),
                            minValArray(i*4+3))
      gl.glUniform4f(maxId, maxValArray(i*4+0),
                            maxValArray(i*4+1),
                            maxValArray(i*4+2),
                            maxValArray(i*4+3))
    }

    // Actually do the draw
    gl.glCallList(drawListId)
    gl.glFlush

    // Disable the texture fb
    disableTextureTarget(gl, vp(2), vp(3))

    // Disable the shader program
    gl.glUseProgram(0)
  }

  /**
   * Enable the texture renderbuffer
   */
  def enableTextureTarget(gl:GL2, texId:Int, texWidth:Int, texHeight:Int) = {

    gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, texId)

    gl.glViewport(0, 0, texWidth, texHeight)

    // Disable the useless attributes
    gl.glDisable(GL.GL_CULL_FACE)
    gl.glDisable(GL.GL_DEPTH_TEST)

    // Now attach the texture to the framebuffer we want
    gl.glBindTexture(GL.GL_TEXTURE_2D, texId)
    gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, texId)
    gl.glFramebufferTexture2D(GL.GL_FRAMEBUFFER, GL.GL_COLOR_ATTACHMENT0,
                              GL.GL_TEXTURE_2D, texId, 0)
    gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, texId)
    gl.glDrawBuffers(1, Array(GL.GL_COLOR_ATTACHMENT0), 0)

    // Make sure the framebuffer is ok
    gl.glCheckFramebufferStatus(GL.GL_FRAMEBUFFER) match {
      case GL2GL3.GL_FRAMEBUFFER_UNDEFINED => 
        throw new Exception("framebuffer undefined")
      case GL.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT => 
        throw new Exception("incomplete attachment")
      case GL.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT => 
        throw new Exception("missing attachment")
      case GL2GL3.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER => 
        throw new Exception("incomplete draw buffer")
      case GL2GL3.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER => 
        throw new Exception("incomplete read buffer")
      case GL.GL_FRAMEBUFFER_UNSUPPORTED => 
        throw new Exception("unsupported buffer")
      case GL2GL3.GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE => 
        throw new Exception("incomplete multisample")
      case GL.GL_FRAMEBUFFER_COMPLETE =>
        // all is well
    }
  }

  /**
   * Disable the texture framebuffer target
   */
  def disableTextureTarget(gl:GL2, windowWidth:Int, windowHeight:Int) = {
    gl.glBindTexture(GL.GL_TEXTURE_2D, 0)
    gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0)

    gl.glViewport(0, 0, windowWidth, windowHeight)
  }

}

class ProsectionVertexShader(numDims:Int) {
  val template = """
  #version 120

  // Attributes
  %s
  attribute float value;

  // Uniforms
  uniform vec2 halfWindowSize;
  uniform mat4 trans;
  uniform int d1;
  uniform int d2;
  %s
  %s

  // Outputs
  varying vec2 screenPos;
  varying float fragValue;
  varying float dimsPassFlag;

  // Function to get data values
  %s

  void main() {
    // Assign all the projected stuff
    vec2 dataPos = vec2(getDimValue(d1), getDimValue(d2));

    // Compute the dimension filters
    %s

    // Figure out how many dims are in bounds
    %s

    fragValue = value;
    gl_Position = trans * vec4(dataPos, 0.0, 1.0);
    screenPos = halfWindowSize * (1 + gl_Position.xy);
  }
  """
  
  override def toString = 
    template.format(
      attribSrc(numDims), 
      minValSrc(numDims),
      maxValSrc(numDims),
      getDimsFuncSrc(numDims, "getDimValue", "data"),
      dimFiltersSrc(numDims),
      numDimsPassSrc(numDims))

  private def attribSrc(numDims:Int) = 
    (0 until Prosection.numVec4(numDims)).map("attribute vec4 data" + _ + ";").
      reduceLeft(_ + "\n" + _)
  private def minValSrc(numDims:Int) =
    (0 until Prosection.numVec4(numDims)).map("uniform vec4 minVal" + _ + ";").
      reduceLeft(_ + "\n" + _)
  private def maxValSrc(numDims:Int) =
    (0 until Prosection.numVec4(numDims)).map("uniform vec4 maxVal" + _ + ";").
      reduceLeft(_ + "\n" + _)

  /**
   * code to compute the filter factors for the dimensions
   */
  private def dimFiltersSrc(numDims:Int) = 
    (0 until Prosection.numVec4(numDims)).map {dd =>
      ("vec4 filterFactor%d = vec4(" + 
          "d1==%d||d2==%d ? 1.0 : 0.0, " +
          "d1==%d||d2==%d ? 1.0 : 0.0, " +
          "d1==%d||d2==%d ? 1.0 : 0.0, " +
          "d1==%d||d2==%d ? 1.0 : 0.0);").format(dd, dd*4+0, dd*4+0, 
                                                     dd*4+1, dd*4+1, 
                                                     dd*4+2, dd*4+2, 
                                                     dd*4+3, dd*4+3)
    }.reduceLeft(_ + "\n" + _)

  /**
   * code to compute the number of dimensions within the limits
   *
   * 0 is the failure value
   */
  private def numDimsPassSrc(numDims:Int) = 
    (0 until Prosection.numVec4(numDims)).map {dd =>
      "vec4 minPass%d = filterFactor%d + step(minVal%d, data%d);".format(dd, dd, dd, dd) + "\n" +
      "vec4 maxPass%d = filterFactor%d + step(data%d, maxVal%d);".format(dd, dd, dd, dd) + "\n" +
      "vec4 dimPass%d = minPass%d * maxPass%d;".format(dd, dd, dd)
    }.reduceLeft(_ + "\n" + _) + "\n" +
    "dimsPassFlag = " +
    (0 until Prosection.numVec4(numDims)).map {dd =>
      "dimPass%d.x * dimPass%d.y * dimPass%d.z * dimPass%d.w".format(dd, dd, dd, dd)
    }.reduceLeft(_ + " * " + _) + ";\n"

  /**
   * Generates the source code to separate the dimension values
   */
  private def getDimsFuncSrc(numDims:Int, func:String, varName:String) = 
    "float %s(int d) {\n".format(func) + 
    (0 until Prosection.numVec4(numDims)).map {dd =>
      "if(d==%d) return %s%d.x;\n".format(dd*4+0, varName, dd) +
      "if(d==%d) return %s%d.y;\n".format(dd*4+1, varName, dd) +
      "if(d==%d) return %s%d.z;\n".format(dd*4+2, varName, dd) +
      "if(d==%d) return %s%d.w;\n".format(dd*4+3, varName, dd)
    }.reduceLeft(_ + "\n" + _) +
    "}\n"
}

