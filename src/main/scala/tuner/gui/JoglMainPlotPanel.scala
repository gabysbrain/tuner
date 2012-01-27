package tuner.gui

import com.jogamp.common.nio.Buffers
import javax.media.opengl.DebugGL2
import javax.media.opengl.TraceGL2
import javax.media.opengl.{GL,GL2,GL2GL3}
import javax.media.opengl.fixedfunc.GLMatrixFunc
import javax.media.opengl.fixedfunc.GLPointerFunc

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
  var textureFbo:Option[Int] = None
  var fboTexture:Option[Int] = None

  var lastResponse:String = ""

  // All the plot transforms
  var plotTransforms = Map[(String,String),Matrix4]()

  def ensureGl(gl:GL) = {
    //val gl2 = new TraceGL2(gl.getGL2, System.out)
    val gl2 = gl.getGL2
    plotTransforms = computePlotTransforms(sliceBounds, width, height)

    setupTextureTarget(gl2, width, height)

    // processing resets the projection matrices
    gl2.glMatrixMode(GLMatrixFunc.GL_PROJECTION)
    gl2.glPushMatrix
    gl2.glLoadIdentity
    gl2.glMatrixMode(GLMatrixFunc.GL_MODELVIEW)
    gl2.glPushMatrix
    gl2.glLoadIdentity

    // Load in the shader programs
    if(!plotShader.isDefined) {
      plotShader = Some(GPPlotGlsl.fromResource(
          gl, project.inputFields.size, 
          "/shaders/plot.frag.glsl"))
      println(plotShader.get.attribIds)
    }

    ensureBuffers(gl.getGL2)
  }

  def disableGl(gl:GL) = {
    // Put the matrices back where we found them
    val gl2 = gl.getGL2
    gl2.glMatrixMode(GLMatrixFunc.GL_PROJECTION)
    gl2.glPopMatrix
    gl2.glMatrixMode(GLMatrixFunc.GL_MODELVIEW)
    gl2.glPopMatrix

    // No more shaders
    gl.getGL2ES2.glUseProgram(0)

    // No more vertex buffers
    gl.getGL2.glBindBuffer(GL.GL_ARRAY_BUFFER, 0)
    gl2.glDisableClientState(GLPointerFunc.GL_VERTEX_ARRAY)
  }

  def setupTextureTarget(gl:GL2, texWidth:Int, texHeight:Int) = {
    // First setup the overall framebuffer
    val fbo = Array(0)
    gl.glGenFramebuffers(1, fbo, 0)
    textureFbo = Some(fbo(0))
    gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, textureFbo.get)

    // Create a texture in which to render
    val tex = Array(0)
    gl.glGenTextures(1, tex, 0)
    fboTexture = Some(tex(0))

    gl.glBindTexture(GL.GL_TEXTURE_2D, fboTexture.get)
    gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR)
    gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR)
    gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE)
    gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE)
    gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, 1, width, height, 0, 
                    GL2GL3.GL_RED, GL.GL_FLOAT, 0)

    // Now attach the texture to the framebuffer we want
    gl.glFramebufferTexture2D(GL.GL_FRAMEBUFFER, GL.GL_COLOR_ATTACHMENT0,
                              GL.GL_TEXTURE_2D, fboTexture.get, 0)
  }

  def cleanTextureTarget(gl:GL2) = {
    gl.glDeleteTextures(1, Array(fboTexture.get), 0)
    gl.glDeleteFramebuffers(1, Array(textureFbo.get), 0)
  }

  def ensureBuffers(gl:GL2) = {
    //val vao = Array(0)
    //gl.glGenVertexArrays(1, vao, 0)
    //vertexArray = Some(vao(0))

    if(!vertexBuffer.isDefined) {
      val vbo = Array(0)
      //gl.glGenBuffers(1, vbo, 0)
      //vertexBuffer = Some(vbo(0))
      //setupPlotVertices(gl)
    }
  }

  def setupPlotVertices(gl:GL2) = {

    // Need one float per dim and value plus one for the
    // response value plus the geometry offsets
    val fields = project.inputFields
    val padFields = GPPlotGlsl.padCount(fields.size)
    //val pointSize = fields.size + padFields + 2 + 1
    //val numFloats = 6 * project.designSites.numRows * pointSize
    val pointSize = 4
    val numFloats = project.designSites.numRows * pointSize
    val tmpBuf = Buffers.newDirectFloatBuffer(numFloats)
    for(r <- 0 until project.designSites.numRows) {
      val tpl = project.designSites.tuple(r)
      /*
      List((-1f,1f),(-1f,-1f),(1f,1f),(-1f,-1f),(1f,1f),(1f,-1f)).foreach{pt =>
        fields.foreach {fld => tmpBuf.put(tpl(fld))}
        (0 until padFields).foreach {_ => tmpBuf.put(0f)}
        tmpBuf.put(pt._1)
        tmpBuf.put(pt._2)
      }
      */
      //println("p: " + tpl(fields(0)) + " " + tpl(fields(1)))
      tmpBuf.put(tpl(fields(0)))
      tmpBuf.put(tpl(fields(1)))
      tmpBuf.put(0f)
      tmpBuf.put(1f)
    }
    // Put 0s at the end until we're ready to copy in the response values
    /*
    for(r <- 0 until project.designSites.numRows) {
      tmpBuf.put(0f)
    }
    */

    tmpBuf.rewind

    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBuffer.get)
    gl.glBufferData(GL.GL_ARRAY_BUFFER, numFloats * Buffers.SIZEOF_FLOAT,
                    tmpBuf, GL.GL_STATIC_DRAW)
  }

  def updateResponseValues(gl:GL2, response:String) = {
    val respBuf = Buffers.newDirectFloatBuffer(6 * project.designSites.numRows)
    for(r <- 0 until project.designSites.numRows) {
      val tpl = project.designSites.tuple(r)
      respBuf.put(tpl(response))
    }
    respBuf.rewind

    // figure out the offset in the actual vertex buffer
    val fields = project.inputFields
    val padFields = GPPlotGlsl.padCount(fields.size)
    val pointSize = fields.size + padFields + 2
    val offset = 6 * project.designSites.numRows * pointSize
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBuffer.get)
    gl.glBufferSubData(GL.GL_ARRAY_BUFFER, offset, 
                       6 * project.designSites.numRows, respBuf)
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
    //println("===")
    //println(dataTrans)
    //println("---")
    //println(dataScale)

    // Put the bounds in 0,1 terms
    // bounds are defined upside down since that's how processing likes it
    val tmp = Rectangle((bounds.minX, height-bounds.minY),
                        (bounds.maxX, height-bounds.maxY))
    val pctBounds = tmp.flipVertical / (width, height)

    // moves the plots into place
    val plotTrans = Matrix4.translate(pctBounds.minX, pctBounds.minY, 0)
    val plotScale = Matrix4.scale(pctBounds.width, pctBounds.height, 1)

    // The final transformation
    val ttl = projectionMatrix * plotTrans * plotScale * dataTrans * dataScale
    (xFld,yFld) -> ttl
  }

  def setupRenderingState(gl:GL2) = {

    val fields = project.inputFields

    gl.glEnable(GL.GL_BLEND);
    gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA)
    
    // set up all the contexts
    //gl.glEnableClientState(GLPointerFunc.GL_VERTEX_ARRAY)
    gl.glUseProgram(plotShader.get.programId)
    //gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBuffer.get)
    //val ptId = plotShader.get.attribId("vPos")
    //gl.glVertexAttribPointer(ptId, 4, GL.GL_FLOAT, false,
                             //4 * Buffers.SIZEOF_FLOAT, 0)

    // Every 4 fields goes into one attribute
    val sliceArray = (fields.map(project.viewInfo.currentSlice(_)) ++
                      List.fill(GPPlotGlsl.padCount(fields.size))(0f)).toArray
    //println("slice " + sliceArray.toList)
    for(i <- 0 until GPPlotGlsl.numVec4(fields.size)) {
      //val ptId = plotShader.get.attribId("p" + i)
      //gl.glVertexAttribPointer(ptId, 4, GL.GL_FLOAT, false,
                               //(fieldSize + 2) * Buffers.SIZEOF_FLOAT,
                               //i * 4 * Buffers.SIZEOF_FLOAT)

      // Send down the current slice
      val sId = plotShader.get.uniformId("slice" + i)
      gl.glUniform4f(sId, sliceArray(i*4+0), 
                          sliceArray(i*4+1), 
                          sliceArray(i*4+2),
                          sliceArray(i*4+3))

    }

    // figure out the maximum distance to render a point
    val maxSqDist = -math.log(1e-9)
    //gl.glUniform1f(plotShader.get.uniformId("maxSqDist"), maxSqDist.toFloat)

    /*
    // Also assign the geometry offset here
    gl.glVertexAttribPointer(plotShader.get.attribId("geomOffset"),
                             2, GL.GL_FLOAT, false,
                             (fieldSize + 2) * Buffers.SIZEOF_FLOAT,
                             fieldSize * Buffers.SIZEOF_FLOAT)

    // Assign the response
    val respId = plotShader.get.attribId("response")
    gl.glVertexAttribPointer(respId, 1, GL.GL_FLOAT, false,
                             Buffers.SIZEOF_FLOAT,
                             fields.size * Buffers.SIZEOF_FLOAT)
    */
  }

  override protected def drawResponses = {
    // setup the opengl context for drawing
    val pgl = g.asInstanceOf[PGraphicsOpenGL]
    val gl = pgl.beginGL
    //val gl2 = new TraceGL2(gl.getGL2, System.out)
    val gl2 = new DebugGL2(gl.getGL2)
    // Make sure all the opengl stuff is set up
    ensureGl(gl)
    setupRenderingState(gl2)

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
    val gl = g.asInstanceOf[PGraphicsOpenGL].beginGL
    val gl2 = new DebugGL2(gl.getGL2)
    //val gl2 = new TraceGL2(gl.getGL2, System.out)
    val es1 = gl.getGL2ES1

    // Make sure the response value hasn't changed
    /*
    if(response != lastResponse) {
      updateResponseValues(gl2, response)
      lastResponse = response
    }
    */
    val (xr, yr) = if(xRange._1 < yRange._1) {
      (xRange, yRange)
    } else {
      (yRange, xRange)
    }
    val fields = project.inputFields
    val xi = fields.indexOf(xr._1)
    val yi = fields.indexOf(yr._1)
    //println("xr: " + xr + " yr: " + yr)

    // set the uniforms specific to this plot
    val trans = plotTransforms((xFld,yFld))
    val model = project.gpModels(response)
    //println(model.corrResponses.toList)
    //println("trans: " + trans)
    gl2.glUniformMatrix4fv(plotShader.get.uniformId("trans"), 
                           1, false, trans.toArray, 0)
    gl2.glUniform1i(plotShader.get.uniformId("d1"), xi)
    gl2.glUniform1i(plotShader.get.uniformId("d2"), yi)
    gl2.glUniform2f(plotShader.get.uniformId("dataMin"), 
                    xr._2._1, yr._2._1)
    gl2.glUniform2f(plotShader.get.uniformId("dataMax"), 
                    xr._2._2, yr._2._2)
    //println("xr: " + xr)
    //println("yr: " + yr)

    // Send down all the theta values
    val thetaArray = (fields.map(model.theta(_).toFloat) ++
                      List.fill(GPPlotGlsl.padCount(fields.size))(0f)).toArray
    //println("theta " + thetaArray.toList)
    for(i <- 0 until GPPlotGlsl.numVec4(fields.size)) {
      val tId = plotShader.get.uniformId("theta" + i)
      gl2.glUniform4f(tId, thetaArray(i*4 + 0), 
                           thetaArray(i*4+1), 
                           thetaArray(i*4+2),
                           thetaArray(i*4+3))
    }

    // send down the mean and sigma^2
    //gl2.glUniform1f(plotShader.get.uniformId("mean"), model.mean.toFloat)
    gl2.glUniform1f(plotShader.get.uniformId("sig2"), model.sig2.toFloat)
    //println("mean: " + model.mean)
    //println("sig: " + model.sig2)

    es1.glPointSize(7f)
    gl2.glBegin(GL2.GL_QUADS)
    //gl2.glBegin(GL.GL_POINTS)
    val corrResponses = model.corrResponses
    //println("res: " + corrResponses.toList)
    for(r <- 0 until project.designSites.numRows) {
    //for(r <- 55 until 56) {
      val tpl = project.designSites.tuple(r)
      // Draw all the point data
      List((-1f,1f),(-1f,-1f),(1f,-1f),(1f,1f)).foreach{gpt =>
      //List((-1f,-1f)).foreach{gpt =>
        for(i <- 0 until GPPlotGlsl.numVec4(fields.size)) {
          val ptId = plotShader.get.attribId("data" + i)
          val fieldVals = (i*4 until math.min(fields.size, (i+1)*4)).map {j =>
            tpl(fields(j))
          } ++ List(0f, 0f, 0f, 0f)
          
          //println("fv " + fieldVals)
          gl2.glVertexAttrib4f(ptId, fieldVals(0), fieldVals(1), 
                                     fieldVals(2), fieldVals(3))
        }
        //println("res: " + corrResponses(r))
        val offsetId = plotShader.get.attribId("geomOffset")
        gl2.glVertexAttrib2f(offsetId, xr._2._2 * gpt._1, yr._2._2 * gpt._2)
        //gl2.glVertexAttrib2f(offsetId, gpt._1, gpt._2)

        // Need to call this last to flush
        val respId = plotShader.get.attribId("corrResp")
        gl2.glVertexAttrib1f(respId, corrResponses(r).toFloat)
      }
    }
    gl2.glEnd
  }

  /*
  override protected def drawResponse(xFld:String, yFld:String,
                                      xRange:(String,(Float,Float)),
                                      yRange:(String,(Float,Float)),
                                      response:String,
                                      closestSample:Table.Tuple) = {

    //val gl = new g.asInstanceOf[PGraphicsOpenGL].beginGL
    val gl = g.asInstanceOf[PGraphicsOpenGL].beginGL
    val gl2 = new DebugGL2(gl.getGL2)
    //val gl2 = new TraceGL2(gl.getGL2, System.out)
    val es1 = gl.getGL2ES1

    // Make sure the response value hasn't changed
    if(response != lastResponse) {
      updateResponseValues(gl2, response)
      lastResponse = response
    }
    val fields = project.inputFields
    val xi = fields.indexOf(xRange._1)
    val yi = fields.indexOf(yRange._1)

    // set the uniforms specific to this plot
    val trans = plotTransforms((xFld,yFld))
    //val model = project.gpModels(response)
    gl2.glUniformMatrix4fv(plotShader.get.uniformId("trans"), 
                           1, false, trans.toArray, 0)
    gl2.glUniform1i(plotShader.get.uniformId("d1"), xi)
    gl2.glUniform1i(plotShader.get.uniformId("d2"), yi)
    gl2.glUniform2f(plotShader.get.uniformId("dataMin"), 
                    project.designSites.min(xFld),
                    project.designSites.min(yFld))
    gl2.glUniform2f(plotShader.get.uniformId("dataMax"), 
                    project.designSites.max(xFld),
                    project.designSites.max(yFld))

    // Send down all the theta values
    val thetaArray = (fields.map(model.theta(_).toFloat) ++
                      List.fill(GPPlotGlsl.padCount(fields.size))(0f)).toArray
    for(i <- 0 until GPPlotGlsl.numVec4(fields.size)) {
      val tId = plotShader.get.uniformId("theta" + i)
      gl2.glUniform4f(tId, thetaArray(i*4 + 0), 
                           thetaArray(i*4+1), 
                           thetaArray(i*4+2),
                           thetaArray(i*4+3))
    }

    // Finally, can draw!
    //gl.glDrawArrays(GL.GL_TRIANGLES, 0, project.designSites.numRows * 6)
    es1.glPointSize(5f)
    //gl.glDrawArrays(GL.GL_POINTS, 0, project.designSites.numRows)

    //gl2.glBegin(GL.GL_POINTS)
    //for(i <- 0 until project.designSites.numRows) {
      //val tpl = project.designSites.tuple(i)
      //println("p2: " + tpl(fields(0)) + " " + tpl(fields(1)))
      //gl2.glVertex3f(tpl(fields(xi)), tpl(fields(yi)), 0f)
    //}
    //gl2.glEnd
  }
  */

}

