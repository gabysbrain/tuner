package tuner.gui

import com.jogamp.common.nio.Buffers
import javax.media.opengl.DebugGL2
import javax.media.opengl.TraceGL2
import javax.media.opengl.{GL,GL2,GL2GL3,GL2ES1}
import javax.media.opengl.fixedfunc.GLMatrixFunc
import javax.media.opengl.fixedfunc.GLPointerFunc

import processing.opengl.PGraphicsOpenGL

import tuner.Config
import tuner.SpecifiedColorMap
import tuner.Table
import tuner.ViewInfo
import tuner.geom.Rectangle
import tuner.gui.opengl.Convolver
import tuner.gui.opengl.Glsl
import tuner.gui.opengl.Prosection
import tuner.gui.util.Matrix4
import tuner.project.Viewable

object JoglMainPlotPanel {
  
  def isCapable(gl:GL) = gl.hasGLSL &&
                         gl.isFunctionAvailable("glBindFramebuffer") &&
                         gl.isFunctionAvailable("glDrawBuffers") &&
                         gl.isExtensionAvailable("GL_ARB_texture_float")

}

/**
 * The Hyperslice view of the GP model rendered using native OpenGL
 */
class JoglMainPlotPanel(project:Viewable) 
    extends ProcessingMainPlotPanel(project) {

  val debugGl = true

  val projectionMatrix = Matrix4.translate(-1, -1, 0) * Matrix4.scale(2, 2, 1)

  // Convenient to keep track of the current size
  var panelSize = (0f, 0f)

  // These need to wait for the GL context to be set up
  // We use a separate shader program for each response
  var convolutionShaders:Map[String,Convolver] = Map() // just for estimate
  var prosectionShaders:Map[String,Prosection] = Map()
  var colormapShader:Option[Glsl] = None

  // The buffers we're using
  var valueFbo:Option[Int] = None
  var valueTex:Option[Int] = None

  // All the plot transforms
  var plotTransforms = Map[(String,String),(Matrix4,Matrix4)]()

  def setupGl(gl:GL) = {
    // Make sure opengl can do everything we want it to do
    if(!JoglMainPlotPanel.isCapable(gl))
      throw new Exception("OpenGL not advanced enough")

    val gl2 = new DebugGL2(gl.getGL2)

    // processing resets the projection matrices
    gl2.glMatrixMode(GLMatrixFunc.GL_PROJECTION)
    gl2.glPushMatrix
    gl2.glLoadIdentity
    gl2.glMatrixMode(GLMatrixFunc.GL_MODELVIEW)
    gl2.glPushMatrix
    gl2.glLoadIdentity

    // Create the shader programs
    if(convolutionShaders.isEmpty) {
      convolutionShaders = project.responseFields.map {resFld =>
        val model = project.gpModels(resFld)
        val estShader = Convolver.fromResource(
            gl.getGL2, project.inputFields.size, 
            "/shaders/est.plot.frag.glsl",
            model.mean, model.sig2,
            model.thetas.toArray,
            model.design, model.corrResponses)
        println(estShader.attribIds)
        (resFld -> estShader)
      } toMap
    }
    if(prosectionShaders.isEmpty) {
      prosectionShaders = project.responseFields.map {resFld =>
        val model = project.gpModels(resFld)
        val ptShader = Prosection.fromResource(
            gl.getGL2, project.inputFields.size, model.design, model.responses)
        println(ptShader.attribIds)
        (resFld -> ptShader)
      } toMap
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
    if(!valueFbo.isDefined) {
      val fbo = Array(0)
      gl.glGenFramebuffers(1, fbo, 0)
      valueFbo = Some(fbo(0))
    }

    // Create a texture in which to render
    if(!valueTex.isDefined) {
      val tex = Array(0)
      gl.glGenTextures(1, tex, 0)
      valueTex = Some(tex(0))

      gl.glBindTexture(GL.GL_TEXTURE_2D, valueTex.get)
      gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE)
      gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE)
      gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST)
      gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST)
      val fakeBuffer = Buffers.newDirectFloatBuffer(Array.fill(4*texWidth*texHeight)(0f))
      fakeBuffer.rewind
      gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL2GL3.GL_RGBA32F, 
                      texWidth, texHeight, 0, 
                      GL2GL3.GL_BGRA, GL.GL_FLOAT, fakeBuffer)
      //gl.glGenerateMipmap(GL.GL_TEXTURE_2D)
    }
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
    // response 2 plots are flipped 90 degrees
    val (dataTrans, dataScale) = if(xFld < yFld) {
      (Matrix4.translate(-minX, -minY, 0),
       Matrix4.scale(1/(maxX-minX), 1/(maxY-minY), 1))
    } else {
      (Matrix4.translate(-minY, -minX, 0),
       Matrix4.scale(1/(maxY-minY), 1/(maxX-minX), 1))
    }

    // Put the bounds in 0,1 terms
    // bounds are defined upside down since that's how processing likes it
    val tmp = Rectangle((bounds.minX, height-bounds.minY),
                        (bounds.maxX, height-bounds.maxY))
    val pctBounds = tmp.flipVertical / (width, height)

    // moves the plots into place
    val plotTrans = Matrix4.translate(pctBounds.minX, pctBounds.minY, 0)
    val plotScale = Matrix4.scale(pctBounds.width, pctBounds.height, 1)

    // The final transformations
    val ttlProj = projectionMatrix * dataTrans * dataScale
    val ttlPlot = projectionMatrix * plotTrans * plotScale
    (xFld,yFld) -> (ttlProj, ttlPlot)
  }

  /**
   * Does opengl setup and takedown 
   */
  override protected def drawResponses = {
    val pgl = g.asInstanceOf[PGraphicsOpenGL]

    // Make sure all the opengl stuff is set up
    // only use opengl stuff when looking at value
    if(project.viewInfo.currentMetric == ViewInfo.ValueMetric) {
      // setup the opengl context for drawing
      val gl = pgl.beginGL
      val gl2 = new DebugGL2(gl.getGL2)

      setupGl(gl)
      plotTransforms = computePlotTransforms(sliceBounds, width, height)
      // All plots are the same size
      val plotRect = sliceBounds.head._2
      setupTextureTarget(gl2, plotRect.width.toInt, plotRect.height.toInt)

      // Return to the real world
      pgl.endGL
    }

    // the old looping code works fine
    val drawTimes = super.drawResponses

    if(project.viewInfo.currentMetric == ViewInfo.ValueMetric) {
      val gl = pgl.beginGL
    
      // clean up after ourselves
      disableGl(gl)

      pgl.endGL
    }

    drawTimes
  }

  /**
   * Draw a single continuous plot
   */
  override protected def drawResponse(xRange:(String,(Float,Float)),
                                      yRange:(String,(Float,Float)),
                                      response:String) = {

    // only use the opengl renderer if we're looking at the values
    if(project.viewInfo.currentMetric == ViewInfo.ValueMetric) {
      val pgl = g.asInstanceOf[PGraphicsOpenGL]
      val gl = pgl.beginGL
      val gl2 = new DebugGL2(gl.getGL2)

      val (texTrans, plotTrans) = plotTransforms((xRange._1, yRange._1))

      // First draw to the texture
      project.viewInfo.currentVis match {
        case ViewInfo.Hyperslice =>
          drawEstimationToTexture(gl2, xRange, yRange, response, texTrans)
        case ViewInfo.Splom =>
          drawProsectionToTexture(gl2, xRange, yRange, response, texTrans)
      }

      // Now put the texture on a quad
      val (xFld, yFld) = (xRange._1, yRange._1)
      val cm = if(xFld < yFld) resp1Colormaps else resp2Colormaps
      drawResponseTexturedQuad(gl2, colormap(response, cm), plotTrans)

      pgl.endGL
    } else {
      super.drawResponse(xRange, yRange, response)
    }
  }

  /**
   * This draws the prosection matrix
   */
  def drawProsectionToTexture(gl:GL2, xRange:(String,(Float,Float)),
                                      yRange:(String,(Float,Float)),
                                      response:String,
                                      trans:Matrix4) = {

    val shader = prosectionShaders(response)
    val model = project.gpModels(response)
    val fields = model.dims
    val plotRect = sliceBounds((xRange._1, yRange._1))
    val (xi,yi) = if(xRange._1 < yRange._1) {
      (fields.indexOf(xRange._1), fields.indexOf(yRange._1))
    } else {
      (fields.indexOf(yRange._1), fields.indexOf(xRange._1))
    }
    val slice = fields.map(project.viewInfo.currentSlice(_)).toArray
    val minVals = fields.map(project.viewInfo.currentZoom.min(_)).toArray
    val maxVals = fields.map(project.viewInfo.currentZoom.max(_)).toArray
    shader.draw(gl, valueFbo.get, valueTex.get, 
                    plotRect.width.toInt, plotRect.height.toInt,
                    trans,
                    xRange, yRange, 
                    xi, yi, 
                    minVals, maxVals)
  }

  /**
   * This puts the estimated value in a texture
   */
  def drawEstimationToTexture(gl:GL2, xRange:(String,(Float,Float)),
                                      yRange:(String,(Float,Float)),
                                      response:String,
                                      trans:Matrix4) = {

    val shader = convolutionShaders(response)
    val model = project.gpModels(response)
    val fields = model.dims
    val plotRect = sliceBounds((xRange._1, yRange._1))
    val corrResponses = model.corrResponses
    val (xi,yi) = if(xRange._1 < yRange._1) {
      (fields.indexOf(xRange._1), fields.indexOf(yRange._1))
    } else {
      (fields.indexOf(yRange._1), fields.indexOf(xRange._1))
    }
    val slice = fields.map(project.viewInfo.currentSlice(_)).toArray

    shader.draw(gl, valueFbo.get, valueTex.get, 
                    plotRect.width.toInt, plotRect.height.toInt,
                    trans,
                    xRange, yRange, 
                    xi, yi, 
                    slice)
  }

  /**
   * Processes the texture through the filtered colormap 
   * and draws everything on screen
   */
  def drawResponseTexturedQuad(gl:GL2, 
                               colormap:SpecifiedColorMap, 
                               trans:Matrix4) = {
    val es2 = gl.getGL2ES2

    gl.glDisable(GL.GL_BLEND)
    gl.glEnable(GL.GL_TEXTURE_2D)

    // Activate the texture program
    gl.glUseProgram(colormapShader.get.programId)

    // Bind the texture uniform
    gl.glActiveTexture(GL.GL_TEXTURE0)
    es2.glUniform1i(colormapShader.get.uniformId("values"), 0)

    // Set the colormap properties
    gl.glUniform1f(colormapShader.get.uniformId("filterLevel"), 
                   colormap.filterVal)
    gl.glUniform1i(colormapShader.get.uniformId("invert"), 
                   if(colormap.isInverted) 1 else 0)
    gl.glUniform1f(colormapShader.get.uniformId("minVal"), colormap.minVal)
    gl.glUniform1f(colormapShader.get.uniformId("maxVal"), colormap.maxVal)
    gl.glUniform4f(colormapShader.get.uniformId("minColor"),
                   colormap.minColor.r, 
                   colormap.minColor.g, 
                   colormap.minColor.b, 
                   1f)
    gl.glUniform4f(colormapShader.get.uniformId("maxColor"),
                   colormap.maxColor.r, 
                   colormap.maxColor.g, 
                   colormap.maxColor.b, 
                   1f)
    gl.glUniform4f(colormapShader.get.uniformId("filterColor"),
                   colormap.filterColor.r, 
                   colormap.filterColor.g,
                   colormap.filterColor.b,
                   1f)

    // Enable the texture
    gl.glBindTexture(GL.GL_TEXTURE_2D, valueTex.get)

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

