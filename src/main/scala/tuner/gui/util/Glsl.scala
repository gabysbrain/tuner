package tuner.gui.util

import javax.media.opengl.GL
import javax.media.opengl.GL2

object Glsl {
  def readFile(filename:String) = {
    
  }
}

class Glsl(gl:GL2, vertexSource:String, fragmentSource:String) {

  val vertShaderId = createShader(GL2.GL_VERTEX_PROGRAM_ARB, vertexSource)
  val fragShaderId = createShader(GL2.GL_FRAGMENT_PROGRAM_ARB, fragmentSource)
  val programId = gl.glCreateProgram
  gl.glAttachShader(programId, vertShaderId)
  gl.glAttachShader(programId, fragShaderId)
  gl.glLinkProgram(programId)
  gl.glValidateProgram(programId)

  def createShader(typeId:Int, source:String) = {
    val shaderId = gl.glCreateShader(typeId)
    gl.glShaderSource(shaderId, 1, Array(source), null)
    gl.glCompileShader(shaderId)
    shaderId
  }

}

