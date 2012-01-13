package tuner.gui.util

import javax.media.opengl.GL
import javax.media.opengl.GL2

object Glsl {
  def readResource(name:String) = {
    val stream = getClass.getResourceAsStream(name)
    val outString = new StringBuffer
    val buf:Array[Byte] = Array.fill(1024)(0)
    var len = stream.read(buf)
    while(len > 0) {
      outString.append(buf)
      len = stream.read(buf)
    }
    outString.toString
  }

  def fromResource(gl:GL2, vertex:String, geom:String, fragment:String) = {
    val vertSource = readResource(vertex)
    val geomSource = readResource(geom)
    val fragSource = readResource(fragment)
    new Glsl(gl, vertSource, Some(geomSource), fragSource)
  }

  def fromResource(gl:GL2, vertex:String, fragment:String) = {
    val vertSource = readResource(vertex)
    val fragSource = readResource(fragment)
    new Glsl(gl, vertSource, fragSource)
  }

}

class Glsl(gl:GL2, vertexSource:String, 
                   geometrySource:Option[String], 
                   fragmentSource:String) {

  val vertShaderId = createShader(GL2.GL_VERTEX_PROGRAM_ARB, vertexSource)
  val geomShaderId = geometrySource.map {gs => 
    createShader(GL2.GL_GEOMETRY_PROGRAM_NV, gs)
  }
  val fragShaderId = createShader(GL2.GL_FRAGMENT_PROGRAM_ARB, fragmentSource)
  val programId = gl.glCreateProgram
  gl.glAttachShader(programId, vertShaderId)
  geomShaderId.foreach {gid => gl.glAttachShader(programId, gid)}
  gl.glAttachShader(programId, fragShaderId)
  gl.glLinkProgram(programId)
  gl.glValidateProgram(programId)

  def this(gl:GL2, vertexSource:String, fragmentSource:String) = 
        this(gl, vertexSource, None, fragmentSource)

  def createShader(typeId:Int, source:String) = {
    val shaderId = gl.glCreateShader(typeId)
    gl.glShaderSource(shaderId, 1, Array(source), null)
    gl.glCompileShader(shaderId)
    shaderId
  }

  def attribId(name:String) : Int = gl.glGetAttribLocation(programId, name)

  def uniformId(name:String) : Int = gl.glGetUniformLocation(programId, name)

}

