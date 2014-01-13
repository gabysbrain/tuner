package tuner.gui.opengl

import com.jogamp.common.nio.Buffers
import javax.media.opengl.{GL,GL2,GL2ES2,GL2GL3}
import javax.media.opengl.GLAutoDrawable

import scala.collection.mutable.ArrayBuffer

import tuner.Config
import tuner.Grid2D
import tuner.gui.util.Matrix4
import tuner.project.Viewable

object RawValueShader {
  def fromResource(gl:GL2, vertex:String, fragment:String, project:Viewable) = {
    val vertSource = Glsl.readResource(vertex)
    val fragSource = Glsl.readResource(fragment)

    new RawValueShader(gl, vertSource, fragSource, project)
  }

}

class RawValueShader(gl:GL2, vertSrc:String, fragSrc:String, project:Viewable)
    extends Glsl(gl, vertSrc, None, fragSrc, List()) {
  
  val pointVbo = {
    val tmp = Array(-1)
    es2.glGenBuffers(1, tmp, 0)
    tmp(0)
  }

  def draw(gl:GL2, textureId:Int, texWidth:Int, texHeight:Int, 
                   trans:Matrix4, 
                   xRange:(String,(Float,Float)), yRange:(String,(Float,Float)),
                   xDim:Int, yDim:Int,
                   response:String, 
                   focusPoint:List[(String,Float)]) = {

    val (xr, yr) = if(xRange._1 < yRange._1) {
      (xRange, yRange)
    } else {
      (yRange, xRange)
    }

    // The project knows which view needs to be drawn
    val data = project.sampleGrid2D(xr, yr, response, focusPoint)

    // Put all the data into our vertex array buffer
    es2.glBindBuffer(GL.GL_ARRAY_BUFFER, pointVbo)
    val dataBuf = Buffers.newDirectFloatBuffer(matrixToDataArray(data))
    dataBuf.rewind
    val indxBuf = Buffers.newDirectIntBuffer(matrixToElementArray(data))
    indxBuf.rewind
    es2.glBufferData(GL.GL_ARRAY_BUFFER, data.size*3*Buffers.SIZEOF_FLOAT,
                     dataBuf, GL.GL_DYNAMIC_DRAW)
    val numTriangles = 2 * (data.rows-1) * (data.columns-1)

    // Enable the raw value shader program
    gl.glUseProgram(programId)

    val vp = Array(0, 0, 0, 0)
    gl.glGetIntegerv(GL.GL_VIEWPORT, vp, 0)

    // Draw to a texture
    enableTextureTarget(gl, textureId, texWidth, texHeight)

    // Actually do the draw
    gl.glClearColor(0f, 0f, 0f, 1f)
    gl.glClear(GL.GL_COLOR_BUFFER_BIT)

    gl.glUniformMatrix4fv(uniformId("trans"), 1, false, trans.toOpenGl, 0)

    es2.glBindBuffer(GL.GL_ARRAY_BUFFER, pointVbo)
    es2.glVertexAttribPointer(attribId("dataPt"), 2, GL.GL_FLOAT, false,
                              3*Buffers.SIZEOF_FLOAT, 0)
    es2.glVertexAttribPointer(attribId("dataVal"), 1, GL.GL_FLOAT, false,
                              3*Buffers.SIZEOF_FLOAT, 2*Buffers.SIZEOF_FLOAT)
    es2.glEnableVertexAttribArray(attribId("dataPt"))
    es2.glEnableVertexAttribArray(attribId("dataVal"))
    es2.glDrawElements(GL.GL_TRIANGLES, 3*numTriangles, GL.GL_UNSIGNED_INT, indxBuf)

    disableTextureTarget(gl, vp(2), vp(3))

    // Done with the shader program
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

  /**
   * Converts the Grid2D into something easy for opengl to render
   */
  def matrixToDataArray(mtx:Grid2D) : Array[Float] = {
    val out = new ArrayBuffer[Float]

    // In the data the rows are the x values...
    for(c <- 0 until mtx.columns) {
      for(r <- 0 until mtx.rows) {
        out.append(mtx.rowVal(r), mtx.colVal(c), mtx.get(r, c))
        //println(mtx.rowVal(r), mtx.colVal(c), mtx.get(r, c))
      }
    }

    out.toArray
  }

  /**
   * Creates the triangles we need to draw
   */
  def matrixToElementArray(mtx:Grid2D) : Array[Int] = {
    val out = new ArrayBuffer[Int]
    val rows = mtx.rows

    //println("slice")
    // Push 2 triangles per cell
    // In the data the rows are the x values...
    for(c <- 0 until mtx.columns-1) {
      for(r <- 0 until mtx.rows-1) {
        val ul = r + rows * c
        val ur = ul + 1
        val ll = ul + rows
        val lr = ll + 1
        out.append(ul, ur, ll)
        out.append(ur, ll, lr)
        //println(ul, ur, ll)
        //println(ur, ll, lr)
      }
    }
    //println("done")

    out.toArray
  }
}

