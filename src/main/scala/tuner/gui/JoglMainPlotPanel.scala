package tuner.gui

import com.jogamp.common.nio.Buffers
import javax.media.opengl.{GL,GL2,GL2ES2}
import javax.media.opengl.{DebugGL2,TraceGL2}
import javax.media.opengl.glu.GLU
import javax.media.opengl.GLAutoDrawable
import javax.media.opengl.GLEventListener
import javax.media.opengl.GLProfile
import javax.media.opengl.GLCapabilities
import javax.media.opengl.awt.GLJPanel
import javax.media.opengl.fixedfunc.GLMatrixFunc

import scala.swing.Component
import scala.swing.BorderPanel

import tuner.Config
import tuner.SpecifiedColorMap
import tuner.project.Viewable
import tuner.geom.Rectangle
import tuner.gui.util.GPPlotGlsl
import tuner.gui.util.FacetLayout
import tuner.gui.util.Glsl
import tuner.gui.util.Matrix4
import tuner.gui.widgets.Colorbar

object Jogl {
  val profile = GLProfile.getDefault
  val capabilities = new GLCapabilities(profile)

  if(!canRun) {
    throw new AssertionError("incompatable opengl version")
  }

  def canRun = profile.hasGLSL && profile.isGL2ES2
}

class JoglMainPlotPanel(val project:Viewable)
    extends BorderPanel with MainPlotPanel {

  val debugGl = true

  val component = new Component {
    override lazy val peer = new GLJPanel(Jogl.capabilities) with SuperMixin
  }
  val joglPanel = component.peer

  val projectionMatrix = Matrix4.translate(-1, -1, 0) * Matrix4.scale(2, 2, 1)

  // Convenient to keep track of the current size
  var panelSize = (0f, 0f)

  // These need to wait for the GL context to be set up
  var plotShader:Option[Glsl] = None

  // The buffers we're using
  //var vertexArray:Option[Int] = None
  var vertexBuffer:Option[Int] = None

  // All the plot transforms
  var plotTransforms = Map[(String,String),Matrix4]()

  joglPanel.addGLEventListener(new GLEventListener {
    def reshape(drawable:GLAutoDrawable, x:Int, y:Int, width:Int, height:Int) =
      setup(drawable, width, height)
    def init(drawable:GLAutoDrawable) = {}
    def dispose(drawable:GLAutoDrawable) = {}
    def display(drawable:GLAutoDrawable) = 
      render(drawable, drawable.getWidth, drawable.getHeight)
  })

  this.preferredSize = new java.awt.Dimension(Config.mainPlotDims._1, 
                                              Config.mainPlotDims._2)
  layout(component) = BorderPanel.Position.Center

  def setup(drawable:GLAutoDrawable, width:Int, height:Int) = {
    //drawable.setGL(new TraceGL2(drawable.getGL.getGL2, System.err))
    if(debugGl) drawable.setGL(new DebugGL2(drawable.getGL.getGL2))

    val gl = drawable.getGL.getGL2
    gl.glViewport(0, 0, width, height)
    panelSize = (width, height)

    // Update all the bounding boxes
    updateBounds(width, height)
    val (ss, sb) = FacetLayout.plotBounds(plotBounds, project.inputFields)
    sliceSize = ss
    sliceBounds = sb
    plotTransforms = computePlotTransforms(sliceBounds, width, height)

    // Load in the shader programs
    plotShader = Some(GPPlotGlsl.fromResource(
        drawable, project.inputFields.size, 
        "/shaders/plot.frag.glsl"))

    initBuffers(drawable)
    setupPlotVertices(drawable)
  }

  def render(drawable:GLAutoDrawable, width:Int, height:Int) = {
    if(debugGl) drawable.setGL(new DebugGL2(drawable.getGL.getGL2))

    val gl = drawable.getGL
    gl.glClear(GL.GL_COLOR_BUFFER_BIT)

    // Draw the continuous plots first
    //renderContinuousPlots(drawable)
    /*
    val gl2 = drawable.getGL.getGL2
    gl2.glMatrixMode(GLMatrixFunc.GL_PROJECTION)
    gl2.glLoadMatrixf(projectionMatrix.toArray, 0)
    gl2.glMatrixMode(GLMatrixFunc.GL_MODELVIEW)
    gl2.glLoadIdentity
    gl2.glBegin(GL2.GL_QUADS)
    gl2.glColor3f(1f, 0f, 0f)
    gl2.glVertex3f(0.25f, 0.75f, 0f)
    gl2.glColor3f(1f, 1f, 0f)
    gl2.glVertex3f(0.25f, 0.25f, 0f)
    gl2.glColor3f(0f, 1f, 0f)
    gl2.glVertex3f(0.75f, 0.25f, 0f)
    gl2.glColor3f(0f, 0f, 1f)
    gl2.glVertex3f(0.75f, 0.75f, 0f)
    gl2.glEnd
    */

    // Now draw the colormaps
    renderLegends(drawable)
  }

  def redraw = {}

  def initBuffers(drawable:GLAutoDrawable) = {
    val gl = drawable.getGL.getGL2
    //val vao = Array(0)
    //gl.glGenVertexArrays(1, vao, 0)
    //vertexArray = Some(vao(0))

    val vbo = Array(0)
    gl.glGenBuffers(1, vbo, 0)
    vertexBuffer = Some(vbo(0))
  }

  def setupPlotVertices(drawable:GLAutoDrawable) = {
    val gl = drawable.getGL.getGL2

    // Need one float per dim and value plus one for each of 
    // 2 responses plus the geometry offsets
    val fields = project.inputFields
    val padFields = GPPlotGlsl.padCount(fields.size)
    val pointSize = fields.size + padFields + 2 + 2
    val numFloats = 6 * project.designSites.numRows * pointSize
    val tmpBuf = Buffers.newDirectFloatBuffer(numFloats)
    for(r <- 0 until project.designSites.numRows) {
      val tpl = project.designSites.tuple(r)
      List((-1f,1f),(-1f,-1f),(1f,1f),(-1f,-1f),(1f,1f),(1f,-1f)).foreach{pt =>
        fields.foreach {fld => tmpBuf.put(tpl(fld))}
        (0 until padFields).foreach {_ => tmpBuf.put(0f)}
        tmpBuf.put(pt._1)
        tmpBuf.put(pt._2)
      }
    }
    project.viewInfo.response1View.foreach {r1Fld =>
      for(r <- 0 until project.designSites.numRows) {
        val tpl = project.designSites.tuple(r)
        tmpBuf.put(tpl(r1Fld))
      }
    }
    project.viewInfo.response1View.foreach {r2Fld =>
      for(r <- 0 until project.designSites.numRows) {
        val tpl = project.designSites.tuple(r)
        tmpBuf.put(tpl(r2Fld))
      }
    }

    tmpBuf.rewind

    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBuffer.get)
    gl.glBufferData(GL.GL_ARRAY_BUFFER, numFloats * Buffers.SIZEOF_FLOAT,
                    tmpBuf, GL.GL_STATIC_DRAW)
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
    val pctBounds = bounds / (width, height)

    // moves the plots into place
    val plotTrans = Matrix4.translate(pctBounds.minX, pctBounds.minY, 0)
    val plotScale = Matrix4.scale(pctBounds.width, pctBounds.height, 1)

    // The final transformation
    val ttl = projectionMatrix * plotTrans * plotScale * dataScale * dataTrans
    (xFld,yFld) -> ttl
  }

  def renderContinuousPlots(drawable:GLAutoDrawable) = {
    val gl = drawable.getGL.getGL2

    val fields = project.inputFields
    val fieldSize = fields.size + GPPlotGlsl.padCount(fields.size)
    val resp1Start = project.designSites.numRows * fields.size
    val resp2Start = project.designSites.numRows * (fields.size+1)

    // set up all the contexts
    gl.glUseProgram(plotShader.get.programId)
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBuffer.get)
    //gl.glBindVertexArray(vertexArray.get)

    // Every 4 fields goes into one attribute
    val sliceArray = (fields.map(project.viewInfo.currentSlice(_)) ++
                      List.fill(GPPlotGlsl.padCount(fields.size))(0f)).toArray
    for(i <- 0 until GPPlotGlsl.numVec4(fields.size)) {
      val ptId = plotShader.get.attribId(drawable, "p" + i)
      //gl.glEnableVertexAttribArray(ptId)
      gl.glVertexAttribPointer(ptId, 4, GL.GL_FLOAT, false,
                               (fieldSize + 2) * Buffers.SIZEOF_FLOAT,
                               i * 4 * Buffers.SIZEOF_FLOAT)

      // Send down the current slice
      val sId = plotShader.get.uniformId(drawable, "slice" + i)
      gl.glUniform4f(sId, sliceArray(i*4 + 0), 
                          sliceArray(i*4+1), 
                          sliceArray(i*4+2),
                          sliceArray(i*4+3))

    }

    // Also assign the geometry offset here
    gl.glVertexAttribPointer(plotShader.get.attribId(drawable, "geomOffset"),
                             2, GL.GL_FLOAT, false,
                             (fieldSize + 2) * Buffers.SIZEOF_FLOAT,
                             fieldSize * Buffers.SIZEOF_FLOAT)

    // Everything else is plot-dependent
    project.inputFields.zipWithIndex.foreach {case (xFld, xi) =>
      project.inputFields.zipWithIndex.foreach {case (yFld, yi) =>
        if(xFld < yFld) project.viewInfo.response1View.foreach {resp =>
          renderSinglePlot(drawable, xFld, yFld, resp, xi, yi)
        }
        if(yFld < xFld) project.viewInfo.response2View.foreach {resp =>
          renderSinglePlot(drawable, xFld, yFld, resp, xi, yi)
        }
      }
    }
  }

  /**
   * Draw a single continuous plot
   */
  def renderSinglePlot(drawable:GLAutoDrawable,
                       xFld:String, yFld:String, respField:String, 
                       xi:Int, yi:Int) = {
    val gl = drawable.getGL.getGL2
    val fields = project.inputFields
    val respId = plotShader.get.attribId(drawable, "response")
    //gl.glEnableVertexAttribArray(respId)
    // Response 1
    if(xFld < yFld) {
      gl.glVertexAttribPointer(respId, 1, GL.GL_FLOAT, false,
                               Buffers.SIZEOF_FLOAT,
                               fields.size * Buffers.SIZEOF_FLOAT)
    }
    // Response 2
    if(xFld > yFld) {
      gl.glVertexAttribPointer(respId, 1, GL.GL_FLOAT, false,
                               Buffers.SIZEOF_FLOAT,
                               (fields.size+1) * Buffers.SIZEOF_FLOAT)
    }

    // set the uniforms specific to this plot
    val trans = plotTransforms((xFld,yFld))
    val model = project.gpModels(respField)
    gl.glUniformMatrix4fv(plotShader.get.uniformId(drawable, "trans"), 
                          1, false, trans.toArray, 0)
    gl.glUniform1i(plotShader.get.uniformId(drawable, "d1"), xi)
    gl.glUniform1i(plotShader.get.uniformId(drawable, "d2"), yi)
    gl.glUniform2f(plotShader.get.uniformId(drawable, "dataMin"), 
                   project.designSites.min(xFld),
                   project.designSites.min(yFld))
    gl.glUniform2f(plotShader.get.uniformId(drawable, "dataMax"), 
                   project.designSites.max(xFld),
                   project.designSites.max(yFld))

    // Send down all the theta values
    val thetaArray = (fields.map(model.theta(_).toFloat) ++
                      List.fill(GPPlotGlsl.padCount(fields.size))(0f)).toArray
    for(i <- 0 until GPPlotGlsl.numVec4(fields.size)) {
      val tId = plotShader.get.uniformId(drawable, "theta" + i)
      gl.glUniform4f(tId, thetaArray(i*4 + 0), 
                          thetaArray(i*4+1), 
                          thetaArray(i*4+2),
                          thetaArray(i*4+3))
    }

    // Finally, can draw!
    gl.glDrawArrays(GL.GL_TRIANGLES, 0, project.designSites.numRows * 6)
  }

  def renderLegends(drawable:GLAutoDrawable) = {
    val gl = drawable.getGL.getGL2

    // Use the fixed functionality for compactness
    gl.glUseProgram(0)

    // Put everything on a 0,1 coordinate scale
    gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION)
    gl.glLoadMatrixf(projectionMatrix.toArray, 0)
    // All the colorbar rendering code is copied from processing 
    // so it needs to be in pixel coordinates
    val modelView = Matrix4.scale(1f/panelSize._1, 1f/panelSize._2, 1f)
    gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW)
    gl.glLoadMatrixf(modelView.toArray, 0)

    project.viewInfo.response1View.foreach {r =>
      renderSingleLegend(drawable, leftColorbarBounds, Colorbar.Left, 
                                   r, colormap(r, resp1Colormaps))
    }
    project.viewInfo.response2View.foreach {r =>
      renderSingleLegend(drawable, rightColorbarBounds, Colorbar.Right,
                                   r, colormap(r, resp2Colormaps))
    }
  }

  def renderSingleLegend(drawable:GLAutoDrawable, bounds:Rectangle, 
                         placement:Colorbar.Placement, respField:String,
                         colormap:SpecifiedColorMap) = {
    val gl = drawable.getGL.getGL2
    val barWidth = bounds.width - 
                   Config.colorbarLabelSpace._2 -
                   Config.colorbarTickSize -
                   Config.colorbarHandleSize._1
    val barHeight = bounds.height -
                    Config.colorbarLabelSpace._1 -
                    Config.smallFontSize -
                    Config.colorbarHandleSize._2 / 2
    val barStartY = bounds.minY + 
                    Config.colorbarLabelSpace._1 + 
                    Config.smallFontSize
    placement match {
      case Colorbar.Left =>
        renderColorbar(gl, bounds.maxX - barWidth, bounds.maxX, 
                           barStartY, barStartY + barHeight,
                           colormap)
      case Colorbar.Right =>
        renderColorbar(gl, bounds.minX, bounds.minX + barWidth, 
                           barStartY, barStartY + barHeight,
                           colormap)
    }
  }

  def renderColorbar(gl:GL2, minX:Float, maxX:Float, 
                             minY:Float, maxY:Float, 
                             colormap:SpecifiedColorMap) = {
    // Maybe draw the filterd out colors
    if(colormap.isFiltered) {
      val yy1 = P5Panel.map(colormap.filterStart, 
                            colormap.minVal, colormap.maxVal, 
                            maxY, minY)
      val yy2 = P5Panel.map(colormap.filterVal, 
                            colormap.minVal, colormap.maxVal, 
                            maxY, minY)
      gl.glColor3f(colormap.filterColor.r, 
                   colormap.filterColor.g, 
                   colormap.filterColor.b)
      gl.glBegin(GL2.GL_QUADS)
      gl.glVertex3f(minX, yy1, 0f)
      gl.glVertex3f(maxX, yy1, 0f)
      gl.glVertex3f(maxX, yy2, 0f)
      gl.glVertex3f(minX, yy2, 0f)
      gl.glEnd
    }
    // Draw the rest of the colormap
    val yy1 = P5Panel.map(colormap.filterVal, 
                          colormap.minVal, colormap.maxVal, 
                          maxY, minY)
    val yy2 = P5Panel.map(colormap.colorEnd, 
                          colormap.minVal, colormap.maxVal, 
                          maxY, minY)
    gl.glBegin(GL2.GL_QUADS)
    val c1 = colormap.color(colormap.filterVal)
    val c2 = colormap.color(colormap.colorEnd)
    gl.glColor3f(c1.r, c1.g, c1.b)
    gl.glVertex3f(minX, yy1, 0f)
    gl.glVertex3f(maxX, yy1, 0f)
    gl.glColor3f(c2.r, c2.g, c2.b)
    gl.glVertex3f(maxX, yy2, 0f)
    gl.glVertex3f(minX, yy2, 0f)
    gl.glEnd
  }
}

