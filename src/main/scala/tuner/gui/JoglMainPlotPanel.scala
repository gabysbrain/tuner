package tuner.gui

import com.jogamp.common.nio.Buffers
import javax.media.opengl.{GL,GL2}
import javax.media.opengl.glu.GLU
import javax.media.opengl.GLAutoDrawable
import javax.media.opengl.GLEventListener
import javax.media.opengl.GLProfile
import javax.media.opengl.GLCapabilities
import javax.media.opengl.awt.GLJPanel

import tuner.project.Viewable
import tuner.geom.Rectangle
import tuner.gui.util.FacetLayout
import tuner.gui.util.Glsl
import tuner.gui.util.Matrix4

object Jogl {
  val profile = GLProfile.getDefault
  val capabilities = new GLCapabilities(profile)

  def canRun = profile.hasGLSL 
}

class JoglMainPlotPanel(val project:Viewable)
    extends GLJPanel(Jogl.capabilities) 
    with MainPlotPanel {

  val projectionMatrix = Matrix4.translate(-1, -1, 0) * Matrix4.scale(2, 2, 1)

  // These need to wait for the GL context to be set up
  var plotShader:Option[Glsl] = None
  var basicShader:Option[Glsl] = None

  // The buffers we're using
  var vertexArray:Option[Int] = None
  var vertexBuffer:Option[Int] = None

  // All the plot transforms
  var plotTransforms = Map[(String,String),Matrix4]()

  addGLEventListener(new GLEventListener {
    def reshape(drawable:GLAutoDrawable, x:Int, y:Int, width:Int, height:Int) =
      setup(drawable.getGL.getGL2, width, height)
    def init(drawable:GLAutoDrawable) = {}
    def dispose(drawable:GLAutoDrawable) = {}
    def display(drawable:GLAutoDrawable) = 
      render(drawable.getGL.getGL2, drawable.getWidth, drawable.getHeight)
  })

  def setup(gl:GL2, width:Int, height:Int) = {
    gl.glViewport(0, 0, width, height)

    // Update all the bounding boxes
    updateBounds(width, height)
    val (ss, sb) = FacetLayout.plotBounds(plotBounds, project.inputFields)
    sliceSize = ss
    sliceBounds = sb
    plotTransforms = computePlotTransforms(sliceBounds, width, height)

    // Load in the shader programs
    plotShader = Some(Glsl.fromResource(gl, "/shaders/plot.vert.glsl",
                                            "/shaders/plot.frag.glsl"))
    //basicShader = new Glsl(gl)

    setupPlotVertices(gl)
  }

  def render(gl:GL2, width:Int, height:Int) = {
    gl.glClear(GL.GL_COLOR_BUFFER_BIT)

    // Draw the continuous plots first
    renderContinuousPlots(gl)
  }

  def initBuffers(gl:GL2) = {
    val vao = Array(0)
    gl.glGenVertexArrays(1, vao, 0)
    vertexArray = Some(vao(0))

    val vbo = Array(0)
    gl.glGenBuffers(1, vbo, 0)
    vertexBuffer = Some(vbo(0))
  }

  def setupPlotVertices(gl:GL2) = {

    // Need one float per dim and value plus one for each of 2 responses
    val fields = project.inputFields
    val pointSize = fields.size + 2
    val numFloats = project.designSites.numRows * pointSize
    val tmpBuf = Buffers.newDirectFloatBuffer(numFloats)
    for(r <- 0 until project.designSites.numRows) {
      val tpl = project.designSites.tuple(r)
      fields.foreach {fld => tmpBuf.put(tpl(fld))}
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

  def renderContinuousPlots(gl:GL2) = {
    val fields = project.inputFields
    val resp1Start = project.designSites.numRows * fields.size
    val resp2Start = project.designSites.numRows * (fields.size+1)

    // set up all the contexts
    gl.glUseProgram(plotShader.get.programId)
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBuffer.get)
    gl.glBindVertexArray(vertexArray.get)

    // Every 4 fields goes into one attribute
    for(i <- 0 until (fields.size / 4.0).ceil.toInt) {
      val vPosId = plotShader.get.attribId("vPos" + i)
      gl.glEnableVertexAttribArray(vPosId)
      gl.glVertexAttribPointer(vPosId, 4, GL.GL_FLOAT, false,
                               fields.size * Buffers.SIZEOF_FLOAT,
                               i * 4 * Buffers.SIZEOF_FLOAT)
    }

    // Send down the current slice
    gl.glUniform1fv(plotShader.get.uniformId("slice"), 
                    fields.size,
                    fields.map(project.viewInfo.currentSlice(_)).toArray,
                    0)

    // Everything else is plot-dependent
    project.inputFields.zipWithIndex.foreach {case (xFld, xi) =>
      project.inputFields.zipWithIndex.foreach {case (yFld, yi) =>
        if(xFld < yFld) project.viewInfo.response1View.foreach {resp =>
          renderSinglePlot(gl, xFld, yFld, resp, xi, yi)
        }
        if(yFld < xFld) project.viewInfo.response2View.foreach {resp =>
          renderSinglePlot(gl, xFld, yFld, resp, xi, yi)
        }
      }
    }
  }

  /**
   * Draw a single continuous plot
   */
  def renderSinglePlot(gl:GL2, 
                       xFld:String, yFld:String, respField:String, 
                       xi:Int, yi:Int) = {
    val fields = project.inputFields
    val respId = plotShader.get.attribId("response")
    gl.glEnableVertexAttribArray(respId)
    // Response 1
    if(xFld < yFld) {
      gl.glVertexAttribPointer(respId, 1, GL.GL_FLOAT, false,
                               Buffers.SIZEOF_FLOAT,
                               fields.size * Buffers.SIZEOF_FLOAT)
    }
    // Response 2
    if(yFld < xFld) {
      gl.glVertexAttribPointer(respId, 1, GL.GL_FLOAT, false,
                               Buffers.SIZEOF_FLOAT,
                               (fields.size+1) * Buffers.SIZEOF_FLOAT)
    }

    // set the uniforms specific to this plot
    val trans = plotTransforms((xFld,yFld))
    val model = project.gpModels(respField)
    gl.glUniformMatrix4fv(plotShader.get.uniformId("trans"), 16, false, 
                          trans.toArray, 0)
    gl.glUniform1i(plotShader.get.uniformId("d1"), xi)
    gl.glUniform1i(plotShader.get.uniformId("d2"), yi)
    gl.glUniform1f(plotShader.get.uniformId("d1Width"), 
                   model.theta(xFld).toFloat)
    gl.glUniform1f(plotShader.get.uniformId("d2Width"), 
                   model.theta(yFld).toFloat)

    // Finally, can draw!
    gl.glDrawArrays(GL.GL_POINTS, 0, project.designSites.numRows)
  }
}

