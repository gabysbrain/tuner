package tuner.gui.util

import com.jogamp.opengl.util.glsl.ShaderUtil
import javax.media.opengl.{GL,GL2,GL2ES2}
import javax.media.opengl.GLAutoDrawable
import javax.media.opengl.GLException

object Glsl {
  def readResource(name:String) = {
    val stream = getClass.getResourceAsStream(name)
    val outString = new StringBuffer
    val buf:Array[Byte] = Array.fill(1024)(0)
    var len = stream.read(buf)
    while(len > 0) {
      outString.append(new String(buf))
      len = stream.read(buf)
    }
    outString.toString.trim
  }

  def fromResource(gl:GL, vertex:String, geom:String, fragment:String) = {
    val vertSource = readResource(vertex)
    val geomSource = readResource(geom)
    val fragSource = readResource(fragment)
    new Glsl(gl, vertSource, Some(geomSource), fragSource)
  }

  def fromResource(gl:GL, vertex:String, fragment:String) = {
    val vertSource = readResource(vertex)
    val fragSource = readResource(fragment)
    new Glsl(gl, vertSource, fragSource)
  }

}

class Glsl(gl:GL,
           vertexSource:String, 
           geometrySource:Option[String], 
           fragmentSource:String) {

  val es2 = gl.getGL2ES2
  val vertShaderId = 
    createShader(es2, GL2ES2.GL_VERTEX_SHADER, vertexSource)
  val geomShaderId = geometrySource.map {gs => 
    createShader(es2, GL2.GL_GEOMETRY_PROGRAM_NV, gs)
  }
  val fragShaderId = 
    createShader(es2, GL2ES2.GL_FRAGMENT_SHADER, fragmentSource)
  val programId = {
    val progId = es2.glCreateProgram
    es2.glAttachShader(progId, vertShaderId)
    geomShaderId.foreach {gid => es2.glAttachShader(progId, gid)}
    es2.glAttachShader(progId, fragShaderId)
    es2.glLinkProgram(progId)
    es2.glValidateProgram(progId)
    val ok = ShaderUtil.isProgramValid(es2, progId) &&
             ShaderUtil.isProgramStatusValid(es2, progId, GL2ES2.GL_LINK_STATUS)
    if(!ok) {
      throw new GLException(ShaderUtil.getProgramInfoLog(es2, progId))
    }
    progId
  }

  def this(gl:GL, vertexSource:String, fragmentSource:String) = 
        this(gl, vertexSource, None, fragmentSource)

  protected def createShader(es2:GL2ES2, typeId:Int, source:String) = {
    val shaderId = es2.glCreateShader(typeId)
    es2.glShaderSource(shaderId, 1, Array(source), null)
    es2.glCompileShader(shaderId)
    val ok = ShaderUtil.isShaderStatusValid(es2, shaderId, GL2ES2.GL_COMPILE_STATUS)
    if(!ok) {
      throw new javax.media.opengl.GLException(
        println(source) + "\n\n" +
        ShaderUtil.getShaderInfoLog(es2, shaderId))
    }
    shaderId
  }

  def attribId(gl:GL, name:String) : Int = {
    val es2 = gl.getGL2ES2
    val id = es2.glGetAttribLocation(programId, name)
    if(id < 0) {
      throw new GLException("attribute " + name + " not found")
    }
    id
  }

  def uniformId(gl:GL, name:String) : Int = {
    val es2 = gl.getGL2ES2
    val id = es2.glGetUniformLocation(programId, name)
    if(id < 0) {
      throw new GLException("uniform " + name + " not found")
    }
    id
  }

}

