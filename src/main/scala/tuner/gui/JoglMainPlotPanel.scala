package tuner.gui

import com.jogamp.common.nio.Buffers
import javax.media.opengl.{GL,GL2}

import processing.opengl.PGraphicsOpenGL

import tuner.Table
import tuner.geom.Rectangle
import tuner.gui.util.Glsl
import tuner.gui.util.GPPlotGlsl
import tuner.gui.util.Matrix4
import tuner.project.Viewable

class JoglMainPlotPanel(project:Viewable) 
    extends ProcessingMainPlotPanel(project) {

  val debugGl = true

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

  def ensureGl(gl:GL) = {
    //drawable.setGL(new TraceGL2(drawable.getGL.getGL2, System.err))
    //if(debugGl) drawable.setGL(new DebugGL2(drawable.getGL.getGL2))

    //gl.glViewport(0, 0, width, height)
    //panelSize = (width, height)

    // Update all the bounding boxes
    //updateBounds(width, height)
    //val (ss, sb) = FacetLayout.plotBounds(plotBounds, project.inputFields)
    //sliceSize = ss
    //sliceBounds = sb
    plotTransforms = computePlotTransforms(sliceBounds, width, height)

    // Load in the shader programs
    plotShader = Some(GPPlotGlsl.fromResource(
        gl, project.inputFields.size, 
        "/shaders/plot.frag.glsl"))

    ensureBuffers(gl.getGL2)
    ensurePlotVertices(gl.getGL2)
  }

  def ensureBuffers(gl:GL2) = {
    //val vao = Array(0)
    //gl.glGenVertexArrays(1, vao, 0)
    //vertexArray = Some(vao(0))

    if(!vertexBuffer.isDefined) {
      val vbo = Array(0)
      gl.glGenBuffers(1, vbo, 0)
      vertexBuffer = Some(vbo(0))
    }
  }

  def ensurePlotVertices(gl:GL2) = {

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

  def setupRenderingState(gl:GL2) = {

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
      val ptId = plotShader.get.attribId(gl, "p" + i)
      //gl.glEnableVertexAttribArray(ptId)
      gl.glVertexAttribPointer(ptId, 4, GL.GL_FLOAT, false,
                               (fieldSize + 2) * Buffers.SIZEOF_FLOAT,
                               i * 4 * Buffers.SIZEOF_FLOAT)

      // Send down the current slice
      val sId = plotShader.get.uniformId(gl, "slice" + i)
      gl.glUniform4f(sId, sliceArray(i*4 + 0), 
                          sliceArray(i*4+1), 
                          sliceArray(i*4+2),
                          sliceArray(i*4+3))

    }

    // Also assign the geometry offset here
    gl.glVertexAttribPointer(plotShader.get.attribId(gl, "geomOffset"),
                             2, GL.GL_FLOAT, false,
                             (fieldSize + 2) * Buffers.SIZEOF_FLOAT,
                             fieldSize * Buffers.SIZEOF_FLOAT)
  }

  /**
   * Draw a single continuous plot
   */
  override protected def drawResponse(xFld:String, yFld:String,
                                      xRange:(String,(Float,Float)),
                                      yRange:(String,(Float,Float)),
                                      response:String,
                                      closestSample:Table.Tuple) = {
    val gl = g.asInstanceOf[PGraphicsOpenGL].beginGL
    val gl2 = gl.getGL2
    // Make sure all the opengl stuff is set up
    ensureGl(gl)

    val fields = project.inputFields
    val xi = fields.indexOf(xRange._1)
    val yi = fields.indexOf(yRange._1)
    val respId = plotShader.get.attribId(gl, "response")
    gl2.glVertexAttribPointer(respId, 1, GL.GL_FLOAT, false,
                              Buffers.SIZEOF_FLOAT,
                              fields.size * Buffers.SIZEOF_FLOAT)

    // set the uniforms specific to this plot
    val trans = plotTransforms((xFld,yFld))
    val model = project.gpModels(response)
    gl2.glUniformMatrix4fv(plotShader.get.uniformId(gl, "trans"), 
                           1, false, trans.toArray, 0)
    gl2.glUniform1i(plotShader.get.uniformId(gl, "d1"), xi)
    gl2.glUniform1i(plotShader.get.uniformId(gl, "d2"), yi)
    gl2.glUniform2f(plotShader.get.uniformId(gl, "dataMin"), 
                    project.designSites.min(xFld),
                    project.designSites.min(yFld))
    gl2.glUniform2f(plotShader.get.uniformId(gl, "dataMax"), 
                    project.designSites.max(xFld),
                    project.designSites.max(yFld))

    // Send down all the theta values
    val thetaArray = (fields.map(model.theta(_).toFloat) ++
                      List.fill(GPPlotGlsl.padCount(fields.size))(0f)).toArray
    for(i <- 0 until GPPlotGlsl.numVec4(fields.size)) {
      val tId = plotShader.get.uniformId(gl, "theta" + i)
      gl2.glUniform4f(tId, thetaArray(i*4 + 0), 
                           thetaArray(i*4+1), 
                           thetaArray(i*4+2),
                           thetaArray(i*4+3))
    }

    // Finally, can draw!
    gl.glDrawArrays(GL.GL_TRIANGLES, 0, project.designSites.numRows * 6)
  }

}

