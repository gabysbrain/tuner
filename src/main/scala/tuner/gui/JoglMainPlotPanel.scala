package tuner.gui

import com.jogamp.common.nio.Buffers
import javax.media.opengl.DebugGL2
import javax.media.opengl.TraceGL2
import javax.media.opengl.{GL,GL2,GL2GL3,GL2ES1}
import javax.media.opengl.fixedfunc.GLMatrixFunc
import javax.media.opengl.fixedfunc.GLPointerFunc

import processing.opengl.PGraphicsOpenGL

import tuner.SpecifiedColorMap
import tuner.Table
import tuner.geom.Rectangle
import tuner.gui.util.Glsl
import tuner.gui.util.GPPlotGlsl
import tuner.gui.util.Matrix4
import tuner.project.Viewable

object JoglMainPlotPanel {
  
  def isCapable(gl:GL) = gl.hasGLSL &&
                         gl.isFunctionAvailable("glBindFramebuffer") &&
                         gl.isFunctionAvailable("glDrawBuffers") &&
                         gl.isExtensionAvailable("GL_ARB_texture_float")

}

class JoglMainPlotPanel(project:Viewable) 
    extends ProcessingMainPlotPanel(project) {

  val debugGl = true

  val projectionMatrix = Matrix4.translate(-1, -1, 0) * Matrix4.scale(2, 2, 1)

  // Convenient to keep track of the current size
  var panelSize = (0f, 0f)

  // These need to wait for the GL context to be set up
  var convolutionShader:Option[Glsl] = None
  var colormapShader:Option[Glsl] = None

  // The buffers we're using
  var textureFbo:Option[Int] = None
  var fboTexture:Option[Int] = None

  var lastResponse:String = ""

  // All the plot transforms
  var plotTransforms = Map[(String,String),(Matrix4,Matrix4)]()

  def setupGl(gl:GL) = {
    // Make sure opengl can do everything we want it to do
    if(!JoglMainPlotPanel.isCapable(gl))
      throw new Exception("OpenGL not advanced enough")

    //val gl2 = new TraceGL2(gl.getGL2, System.out)
    //val gl2 = gl.getGL2
    //val gl2 = new DebugGL2(new TraceGL2(gl.getGL2, System.out))
    val gl2 = new DebugGL2(gl.getGL2)

    // processing resets the projection matrices
    gl2.glMatrixMode(GLMatrixFunc.GL_PROJECTION)
    gl2.glPushMatrix
    gl2.glLoadIdentity
    gl2.glMatrixMode(GLMatrixFunc.GL_MODELVIEW)
    gl2.glPushMatrix
    gl2.glLoadIdentity

    // Create the shader programs
    if(!convolutionShader.isDefined) {
      convolutionShader = Some(GPPlotGlsl.fromResource(
          gl, project.inputFields.size, 
          "/shaders/plot.frag.glsl"))
      println(convolutionShader.get.attribIds)
    }
    if(!colormapShader.isDefined) {
      colormapShader = Some(Glsl.fromResource(
        gl, "/shaders/cmap.vert.glsl", 
            "/shaders/cmap.frag.glsl"))
      println(colormapShader.get.attribIds)
    }
  }

  def disableGl(gl:GL) = {
    // Make sure there are no remaining errors
    var errCode = gl.glGetError
    while(errCode != GL.GL_NONE) {
      println("err: " + errCode)
      errCode = gl.glGetError
    }

    // Put the matrices back where we found them
    val gl2 = gl.getGL2
    gl2.glMatrixMode(GLMatrixFunc.GL_PROJECTION)
    gl2.glPopMatrix
    gl2.glMatrixMode(GLMatrixFunc.GL_MODELVIEW)
    gl2.glPopMatrix

    // No more shaders
    gl.getGL2ES2.glUseProgram(0)

    // No more texture
    gl.glBindTexture(GL.GL_TEXTURE_2D, 0)
  }

  /**
   * Create the texture and framebuffer objects
   */
  def setupTextureTarget(gl:GL2, texWidth:Int, texHeight:Int) = {
    // First setup the overall framebuffer
    if(!textureFbo.isDefined) {
      val fbo = Array(0)
      gl.glGenFramebuffers(1, fbo, 0)
      textureFbo = Some(fbo(0))
    }

    // Create a texture in which to render
    if(!fboTexture.isDefined) {
      val tex = Array(0)
      gl.glGenTextures(1, tex, 0)
      fboTexture = Some(tex(0))

      gl.glBindTexture(GL.GL_TEXTURE_2D, fboTexture.get)
      gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE)
      gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE)
      gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR)
      gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR)
      val fakeBuffer = Buffers.newDirectFloatBuffer(Array.fill(4*texWidth*texHeight)(0f))
      fakeBuffer.rewind
      gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL2GL3.GL_RGBA32F, 
                      texWidth, texHeight, 0, 
                      //GL2GL3.GL_BGRA, GL.GL_FLOAT, fakeBuffer)
                      GL2GL3.GL_BGRA, GL.GL_UNSIGNED_BYTE, fakeBuffer)
      //gl.glGenerateMipmap(GL.GL_TEXTURE_2D)
    }
  }

  /**
   * Enable the texture renderbuffer
   */
  def enableTextureTarget(gl:GL2, texWidth:Int, texHeight:Int) = {

    gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, textureFbo.get)

    gl.glViewport(0, 0, texWidth, texHeight)

    // Disable the useless attributes
    gl.glDisable(GL.GL_CULL_FACE)
    gl.glDisable(GL.GL_DEPTH_TEST)

    // Now attach the texture to the framebuffer we want
    gl.glBindTexture(GL.GL_TEXTURE_2D, fboTexture.get)
    gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, textureFbo.get)
    gl.glFramebufferTexture2D(GL.GL_FRAMEBUFFER, GL.GL_COLOR_ATTACHMENT0,
                              GL.GL_TEXTURE_2D, fboTexture.get, 0)
    gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, textureFbo.get)
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
  def disableTextureTarget(gl:GL2) = {
    gl.glBindTexture(GL.GL_TEXTURE_2D, 0)
    gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0)

    gl.glViewport(0, 0, width, height)
  }

  /**
   * The plots themselves will be drawn in data space so these 
   * move everything into the proper coordinate system
   */
  def computePlotTransforms(
        sb:Map[(String,String),Rectangle], 
        width:Float, height:Float) = sb.map {case ((xFld,yFld),bounds) =>
    val (minX,maxX) = project.viewInfo.currentZoom.range(xFld)
    val (minY,maxY) = project.viewInfo.currentZoom.range(yFld)

    // transforms to move from data space to 0,1 space
    val dataTrans = Matrix4.translate(-minX, -minY, 0)
    val dataScale = Matrix4.scale(1/(maxX-minX), 1/(maxY-minY), 1)

    // Put the bounds in 0,1 terms
    // bounds are defined upside down since that's how processing likes it
    val tmp = Rectangle((bounds.minX, height-bounds.minY),
                        (bounds.maxX, height-bounds.maxY))
    val pctBounds = tmp.flipVertical / (width, height)

    // moves the plots into place
    val plotTrans = Matrix4.translate(pctBounds.minX, pctBounds.minY, 0)
    val plotScale = Matrix4.scale(pctBounds.width, pctBounds.height, 1)

    // The final transformations
    //val ttl = projectionMatrix * plotTrans * plotScale * dataTrans * dataScale
    val ttlProj = projectionMatrix * dataTrans * dataScale
    val ttlPlot = projectionMatrix * plotTrans * plotScale
    (xFld,yFld) -> (ttlProj, ttlPlot)
  }

  /**
   * Does opengl setup and takedown 
   */
  override protected def drawResponses = {
    // setup the opengl context for drawing
    val pgl = g.asInstanceOf[PGraphicsOpenGL]
    val gl = pgl.beginGL
    //val gl2 = new TraceGL2(gl.getGL2, System.out)
    val gl2 = new DebugGL2(gl.getGL2)

    // Make sure all the opengl stuff is set up
    setupGl(gl)
    plotTransforms = computePlotTransforms(sliceBounds, width, height)
    // All plots are the same size
    val plotRect = sliceBounds.head._2
    setupTextureTarget(gl2, plotRect.width.toInt, plotRect.height.toInt)

    // the old looping code works fine
    super.drawResponses
    
    // clean up after ourselves
    disableGl(gl)

    pgl.endGL
  }

  /**
   * Draw a single continuous plot
   */
  override protected def drawResponse(xFld:String, yFld:String,
                                      xRange:(String,(Float,Float)),
                                      yRange:(String,(Float,Float)),
                                      response:String,
                                      closestSample:Table.Tuple) = {

    //val gl = new g.asInstanceOf[PGraphicsOpenGL].beginGL
    val pgl = g.asInstanceOf[PGraphicsOpenGL]
    val gl = pgl.beginGL
    val gl2 = new DebugGL2(gl.getGL2)
    val es2 = gl.getGL2ES2
    //val gl2 = new TraceGL2(gl.getGL2, System.out)
    //val es1 = gl.getGL2ES1

    // Make sure the response value hasn't changed
    /*
    if(response != lastResponse) {
      updateResponseValues(gl2, response)
      lastResponse = response
    }
    */

    val (texTrans, plotTrans) = plotTransforms((xRange._1,yRange._1))

    // First draw to the texture
    drawResponseToTexture(gl2, xRange, yRange, response, texTrans)

    // Now put the texture on a quad
    val cm = if(xFld < yFld) resp1Colormaps else resp2Colormaps
    drawResponseTexturedQuad(gl2, colormap(response, cm), plotTrans)

    pgl.endGL
  }

  def drawResponseToTexture(gl:GL2, xRange:(String,(Float,Float)),
                                    yRange:(String,(Float,Float)),
                                    response:String,
                                    trans:Matrix4) = {
    val fields = project.inputFields
    val (xr, yr) = if(xRange._1 < yRange._1) {
      (xRange, yRange)
    } else {
      (yRange, xRange)
    }
    val xi = fields.indexOf(xr._1)
    val yi = fields.indexOf(yr._1)

    gl.glEnable(GL.GL_BLEND)
    gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA)
    
    // Enable shader program
    gl.glUseProgram(convolutionShader.get.programId)

    // Draw to a texture
    enableTextureTarget(gl, width, height)

    gl.glClearColor(1f, 0f, 0f, 1f)
    gl.glClear(GL.GL_COLOR_BUFFER_BIT)

    // Send down the uniforms for this set
    // Every 4 fields goes into one attribute
    val sliceArray = (fields.map(project.viewInfo.currentSlice(_)) ++
                      List.fill(GPPlotGlsl.padCount(fields.size))(0f)).toArray
    for(i <- 0 until GPPlotGlsl.numVec4(fields.size)) {
      // Send down the current slice
      val sId = convolutionShader.get.uniformId("slice" + i)
      gl.glUniform4f(sId, sliceArray(i*4+0), 
                          sliceArray(i*4+1), 
                          sliceArray(i*4+2),
                          sliceArray(i*4+3))

    }

    // figure out the maximum distance to render a point
    val maxSqDist = -math.log(1e-9)
    //gl.glUniform1f(convolutionShader.get.uniformId("maxSqDist"), maxSqDist.toFloat)

    val model = project.gpModels(response)
    gl.glUniformMatrix4fv(convolutionShader.get.uniformId("trans"), 
                          1, false, trans.toArray, 0)
    gl.glUniform1i(convolutionShader.get.uniformId("d1"), xi)
    gl.glUniform1i(convolutionShader.get.uniformId("d2"), yi)
    gl.glUniform2f(convolutionShader.get.uniformId("dataMin"), 
                   xr._2._1, yr._2._1)
    gl.glUniform2f(convolutionShader.get.uniformId("dataMax"), 
                   xr._2._2, yr._2._2)

    // Send down all the theta values
    val thetaArray = (fields.map(model.theta(_).toFloat) ++
                      List.fill(GPPlotGlsl.padCount(fields.size))(0f)).toArray
    for(i <- 0 until GPPlotGlsl.numVec4(fields.size)) {
      val tId = convolutionShader.get.uniformId("theta" + i)
      gl.glUniform4f(tId, thetaArray(i*4 + 0), 
                          thetaArray(i*4+1), 
                          thetaArray(i*4+2),
                          thetaArray(i*4+3))
    }

    // send down the mean and sigma^2
    //gl.glUniform1f(convolutionShader.get.uniformId("mean"), model.mean.toFloat)
    gl.glUniform1f(convolutionShader.get.uniformId("sig2"), model.sig2.toFloat)


    // Actually do the draw
    gl.glBegin(GL2.GL_QUADS)
    val corrResponses = model.corrResponses
    for(r <- 0 until project.designSites.numRows) {
      val tpl = project.designSites.tuple(r)
      // Draw all the point data
      List((-1f,1f),(-1f,-1f),(1f,-1f),(1f,1f)).foreach{gpt =>
        for(i <- 0 until GPPlotGlsl.numVec4(fields.size)) {
          val ptId = convolutionShader.get.attribId("data" + i)
          val fieldVals = (i*4 until math.min(fields.size, (i+1)*4)).map {j =>
            tpl(fields(j))
          } ++ List(0f, 0f, 0f, 0f)
          
          gl.glVertexAttrib4f(ptId, fieldVals(0), fieldVals(1), 
                                     fieldVals(2), fieldVals(3))
        }
        val offsetId = convolutionShader.get.attribId("geomOffset")
        gl.glVertexAttrib2f(offsetId, xr._2._2 * gpt._1, yr._2._2 * gpt._2)

        // Need to call this last to flush
        val respId = convolutionShader.get.attribId("corrResp")
        gl.glVertexAttrib1f(respId, corrResponses(r).toFloat)
      }
    }
    gl.glEnd
    gl.glFlush

    // Disable the texture fb
    disableTextureTarget(gl)

    // Disable the shader program
    gl.glUseProgram(0)
  }

  def drawResponseTexturedQuad(gl:GL2, 
                               colormap:SpecifiedColorMap, 
                               trans:Matrix4) = {
    val es2 = gl.getGL2ES2

    gl.glEnable(GL.GL_TEXTURE_2D)

    // Activate the texture program
    gl.glUseProgram(colormapShader.get.programId)

    // Bind the texture uniform
    gl.glActiveTexture(GL.GL_TEXTURE0)
    es2.glUniform1i(colormapShader.get.uniformId("values"), 0)

    // Set the colormap properties
    val (minColor, maxColor) = (colormap.startColor, colormap.endColor)
    gl.glUniform1f(colormapShader.get.uniformId("filterLevel"), 
                   colormap.filterVal)
    gl.glUniform1f(colormapShader.get.uniformId("maxVal"),
                   colormap.colorEnd)
    gl.glUniform3f(colormapShader.get.uniformId("minColor"),
                   minColor.r, minColor.g, minColor.b)
    gl.glUniform3f(colormapShader.get.uniformId("maxColor"),
                   maxColor.r, maxColor.g, maxColor.b)

    // Enable the texture
    gl.glBindTexture(GL.GL_TEXTURE_2D, fboTexture.get)

    gl.glUniformMatrix4fv(colormapShader.get.uniformId("trans"), 
                          1, false, trans.toArray, 0)

    gl.glBegin(GL2.GL_QUADS)
    gl.glVertex3f(0f, 0f, 0f)
    gl.glVertex3f(1f, 0f, 0f)
    gl.glVertex3f(1f, 1f, 0f)
    gl.glVertex3f(0f, 1f, 0f)
    gl.glEnd

    //gl.glPopMatrix
    gl.glBindTexture(GL.GL_TEXTURE_2D, 0)
    gl.glDisable(GL.GL_TEXTURE_2D)
    gl.glUseProgram(0)
  }

}

