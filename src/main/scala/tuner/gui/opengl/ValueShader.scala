package tuner.gui.opengl

import javax.media.opengl.{GL,GL2,GL2ES2,GL2GL3}
import javax.media.opengl.GLAutoDrawable

import tuner.Config
import tuner.gui.util.Matrix4

/**
 * Special loader for the continuous plot stuff since 
 * the vertex shader gets dynamically created
 */
object Convolver {
  def fromResource(gl:GL2, numDims:Int, fragment:String,
                           baseValue:Double, globalFactor:Double, 
                           distanceWeights:Array[Double], 
                           points:Array[Array[Double]], 
                           coefficients:Array[Double]) = {
    val fragSource = Glsl.readResource(fragment)

    new Convolver(gl, numDims, fragSource,
                      baseValue, globalFactor, 
                      distanceWeights, 
                      points, coefficients)
  }

  def numVec4(numDims:Int) = (numDims / 4.0).ceil.toInt
  def padCount(numDims:Int) = (4 - (numDims%4)) % 4

}

class Convolver(gl:GL2, numDims:Int, fragment:String,
                        baseValue:Double, globalFactor:Double, 
                        distanceWeights:Array[Double], 
                        points:Array[Array[Double]], 
                        coefficients:Array[Double])
    extends Glsl(gl, new ConvolutionVertexShader(numDims).toString,
                     None, fragment, List()) {
  
  // The maximum distance to render a point
  val maxSqDist = -math.log(Config.maxSampleSqDistance)

  // Setup the display list
  val drawListId = gl.glGenLists(1)
  gl.glNewList(drawListId, GL2.GL_COMPILE)
    gl.glUniform1f(uniformId("maxSqDist"), maxSqDist.toFloat)

    // Send down all the theta values
    val thetaArray = distanceWeights ++ Array(0.0, 0.0, 0.0, 0.0)
    for(i <- 0 until Convolver.numVec4(numDims)) {
      val tId = uniformId("theta" + i)
      gl.glUniform4f(tId, thetaArray(i*4+0).toFloat, 
                          thetaArray(i*4+1).toFloat, 
                          thetaArray(i*4+2).toFloat,
                          thetaArray(i*4+3).toFloat)
    }

    // send down the sigma^2
    gl.glUniform1f(uniformId("sig2"), globalFactor.toFloat)


    // Actually do the draw
    gl.glClearColor(baseValue.toFloat, 0f, 0f, 1f)
    gl.glClear(GL.GL_COLOR_BUFFER_BIT)
    gl.glBegin(GL2.GL_QUADS)
    for(r <- 0 until points.size) {
      val pt = points(r)
      // Draw all the point data
      List((-1f,1f),(-1f,-1f),(1f,-1f),(1f,1f)).foreach{gpt =>
        for(i <- 0 until Convolver.numVec4(numDims)) {
          val ptId = attribId("data" + i)
          val fieldVals = pt ++ Array(0.0, 0.0, 0.0, 0.0)
          
          gl.glVertexAttrib4f(ptId, fieldVals(i*4+0).toFloat, 
                                    fieldVals(i*4+1).toFloat, 
                                    fieldVals(i*4+2).toFloat, 
                                    fieldVals(i*4+3).toFloat)
        }
        val respId = attribId("coeff")
        gl.glVertexAttrib1f(respId, coefficients(r).toFloat)

        // Need to call this last to flush
        val offsetId = attribId("geomOffset")
        gl.glVertexAttrib2f(offsetId, gpt._1, gpt._2)

      }
    }
    gl.glEnd
    gl.glFlush
  gl.glEndList

  def draw(gl:GL2, textureId:Int, texWidth:Int, texHeight:Int,
                   trans:Matrix4,
                   xRange:(String,(Float,Float)), yRange:(String,(Float,Float)),
                   xDim:Int, yDim:Int,
                   pointOfInterest:Array[Float]) = {

    val (xr, yr) = if(xRange._1 < yRange._1) {
      (xRange, yRange)
    } else {
      (yRange, xRange)
    }

    gl.glEnable(GL.GL_BLEND)
    gl.glBlendFunc(GL.GL_ONE, GL.GL_ONE)
    gl.glBlendEquation(GL.GL_FUNC_ADD)
    
    // Enable shader program
    gl.glUseProgram(programId)

    // Save the viewport size before we kill it
    val vp = Array(0, 0, 0, 0)
    gl.glGetIntegerv(GL.GL_VIEWPORT, vp, 0)

    // Draw to a texture
    enableTextureTarget(gl, textureId, texWidth, texHeight)

    gl.glUniformMatrix4fv(uniformId("trans"), 
                          1, false, trans.toArray, 0)
    gl.glUniform1i(uniformId("d1"), xDim)
    gl.glUniform1i(uniformId("d2"), yDim)
    gl.glUniform2f(uniformId("dataMin"), xr._2._1, yr._2._1)
    gl.glUniform2f(uniformId("dataMax"), xr._2._2, yr._2._2)

    // Send down the uniforms for this set
    // Every 4 fields goes into one attribute
    val sliceArray = pointOfInterest ++ Array(0f, 0f, 0f, 0f)
    for(i <- 0 until Convolver.numVec4(numDims)) {
      // Send down the current slice
      val sId = uniformId("slice" + i)
      gl.glUniform4f(sId, sliceArray(i*4+0), 
                          sliceArray(i*4+1), 
                          sliceArray(i*4+2),
                          sliceArray(i*4+3))

    }

    gl.glUniformMatrix4fv(uniformId("trans"), 
                          1, false, trans.toArray, 0)
    gl.glUniform1i(uniformId("d1"), xDim)
    gl.glUniform1i(uniformId("d2"), yDim)
    gl.glUniform2f(uniformId("dataMin"), xr._2._1, yr._2._1)
    gl.glUniform2f(uniformId("dataMax"), xr._2._2, yr._2._2)

    // Draw everything
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

class ConvolutionVertexShader(numDims:Int) {
  val template = """
  #version 120

  // Attributes
  %s
  attribute float coeff;
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
  varying float fragCoeff;
  varying float centerSqDist;
  varying vec2 vertexDist;
  varying vec2 theta;

  // Function to get data values
  %s

  // Function to get slice values
  %s

  // Function to get theta values
  %s

  void main() {
    // Assign all the projected stuff
    vec2 dataPos = vec2(getDimValue(d1), getDimValue(d2));
    vec2 slice = vec2(getSliceValue(d1), getSliceValue(d2));
    theta = vec2(getThetaValue(d1), getThetaValue(d2));
    vec2 dataDist = dataPos - slice;

    // Compute the squared distance
    vec2 wtDataDist = theta * dataDist * dataDist;
    %s

    // This won't get rasterized if the distance is too great
    vec2 actOffset = centerSqDist < maxSqDist ? geomOffset * dataMax : vec2(0.0, 0.0);
    vec2 offset = clamp(dataPos + actOffset, dataMin, dataMax);
    vertexDist = offset - dataPos;
    fragCoeff = coeff;
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
    (0 until Convolver.numVec4(numDims)).map("attribute vec4 data" + _ + ";").
      reduceLeft(_ + "\n" + _)
  private def sliceSrc(numDims:Int) =
    (0 until Convolver.numVec4(numDims)).map("uniform vec4 slice" + _ + ";").
      reduceLeft(_ + "\n" + _)
  private def thetaSrc(numDims:Int) =
    (0 until Convolver.numVec4(numDims)).map("uniform vec4 theta" + _ + ";").
      reduceLeft(_ + "\n" + _)

  /**
   * code to compute the distance between a data point and the slice point
   */
  private def ttlDistSrc(numDims:Int) =
    (0 until Convolver.numVec4(numDims)).map {dd =>
      "vec4 rawDist%d = data%d - slice%d;".format(dd, dd, dd) + "\n" +
      "vec4 sqDist%d = theta%d * rawDist%d * rawDist%d;".format(dd, dd, dd, dd)
    }.reduceLeft(_ + "\n" + _) + "\n" +
    "centerSqDist = " + 
    (0 until Convolver.numVec4(numDims)).map {dd =>
      "sqDist%d.x + sqDist%d.y + sqDist%d.z + sqDist%d.w".format(dd, dd, dd, dd)
    }.reduceLeft(_ + " + " + _) + ";\n" +
    "centerSqDist = centerSqDist - wtDataDist.x - wtDataDist.y;\n"

  /**
   * Generates the source code to separate the dimension values
   */
  private def getDimsFuncSrc(numDims:Int, func:String, varName:String) = 
    "float %s(int d) {\n".format(func) + 
    (0 until Convolver.numVec4(numDims)).map {dd =>
      "if(d==%d) return %s%d.x;\n".format(dd*4+0, varName, dd) +
      "if(d==%d) return %s%d.y;\n".format(dd*4+1, varName, dd) +
      "if(d==%d) return %s%d.z;\n".format(dd*4+2, varName, dd) +
      "if(d==%d) return %s%d.w;\n".format(dd*4+3, varName, dd)
    }.reduceLeft(_ + "\n" + _) +
    "}\n"
}

