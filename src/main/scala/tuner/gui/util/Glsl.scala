package tuner.gui.util

import com.jogamp.opengl.util.glsl.ShaderUtil
import javax.media.opengl.{GL,GL2,GL2ES2, DebugGL2ES2}
import javax.media.opengl.GLAutoDrawable

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

  def fromResource(drawable:GLAutoDrawable, 
                   vertex:String, geom:String, fragment:String) = {
    val vertSource = readResource(vertex)
    val geomSource = readResource(geom)
    val fragSource = readResource(fragment)
    new Glsl(drawable, vertSource, Some(geomSource), fragSource)
  }

  def fromResource(drawable:GLAutoDrawable, 
                   vertex:String, fragment:String) = {
    val vertSource = readResource(vertex)
    val fragSource = readResource(fragment)
    new Glsl(drawable, vertSource, fragSource)
  }

}

class Glsl(drawable:GLAutoDrawable, 
           vertexSource:String, 
           geometrySource:Option[String], 
           fragmentSource:String) {

  val gl = new DebugGL2ES2(drawable.getGL.getGL2ES2)
  val vertShaderId = createShader(GL2ES2.GL_VERTEX_SHADER, vertexSource)
  val geomShaderId = geometrySource.map {gs => 
    createShader(GL2.GL_GEOMETRY_PROGRAM_NV, gs)
  }
  val fragShaderId = createShader(GL2ES2.GL_FRAGMENT_SHADER, fragmentSource)
  val programId = gl.glCreateProgram
  gl.glAttachShader(programId, vertShaderId)
  geomShaderId.foreach {gid => gl.glAttachShader(programId, gid)}
  gl.glAttachShader(programId, fragShaderId)
  gl.glLinkProgram(programId)
  gl.glValidateProgram(programId)

  def this(drawable:GLAutoDrawable, 
           vertexSource:String, fragmentSource:String) = 
        this(drawable, vertexSource, None, fragmentSource)

  def createShader(typeId:Int, source:String) = {
    val shaderId = gl.glCreateShader(typeId)
    gl.glShaderSource(shaderId, 1, Array(source), null)
    gl.glCompileShader(shaderId)
    val ok = ShaderUtil.isShaderStatusValid(gl, shaderId, GL2ES2.GL_COMPILE_STATUS)
    if(!ok) throw new Exception(ShaderUtil.getShaderInfoLog(gl, shaderId))
    shaderId
  }

  def attribId(name:String) : Int = gl.glGetAttribLocation(programId, name)

  def uniformId(name:String) : Int = gl.glGetUniformLocation(programId, name)

}

