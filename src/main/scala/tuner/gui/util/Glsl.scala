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

  val vertShaderId = 
    createShader(drawable, GL2ES2.GL_VERTEX_SHADER, vertexSource)
  val geomShaderId = geometrySource.map {gs => 
    createShader(drawable, GL2.GL_GEOMETRY_PROGRAM_NV, gs)
  }
  val fragShaderId = 
    createShader(drawable, GL2ES2.GL_FRAGMENT_SHADER, fragmentSource)
  val programId = {
    val gl = drawable.getGL.getGL2ES2
    val progId = gl.glCreateProgram
    gl.glAttachShader(progId, vertShaderId)
    geomShaderId.foreach {gid => gl.glAttachShader(progId, gid)}
    gl.glAttachShader(progId, fragShaderId)
    gl.glLinkProgram(progId)
    gl.glValidateProgram(progId)
    val ok = ShaderUtil.isProgramValid(gl, progId) &&
             ShaderUtil.isProgramStatusValid(gl, progId, GL2ES2.GL_LINK_STATUS)
    if(!ok) {
      throw new GLException(ShaderUtil.getProgramInfoLog(gl, progId))
    }
    progId
  }

  def this(drawable:GLAutoDrawable, 
           vertexSource:String, fragmentSource:String) = 
        this(drawable, vertexSource, None, fragmentSource)

  def createShader(drawable:GLAutoDrawable, typeId:Int, source:String) = {
    val gl = drawable.getGL.getGL2ES2
    val shaderId = gl.glCreateShader(typeId)
    gl.glShaderSource(shaderId, 1, Array(source), null)
    gl.glCompileShader(shaderId)
    val ok = ShaderUtil.isShaderStatusValid(gl, shaderId, GL2ES2.GL_COMPILE_STATUS)
    if(!ok) {
      throw new javax.media.opengl.GLException(
        println(source) + "\n\n" +
        ShaderUtil.getShaderInfoLog(gl, shaderId))
    }
    shaderId
  }

  def attribId(drawable:GLAutoDrawable, name:String) : Int = {
    val gl = drawable.getGL.getGL2ES2
    val id = gl.glGetAttribLocation(programId, name)
    if(id < 0) {
      throw new GLException("attribute " + name + " not found")
    }
    id
  }

  def uniformId(drawable:GLAutoDrawable, name:String) : Int = {
    val gl = drawable.getGL.getGL2ES2
    val id = gl.glGetUniformLocation(programId, name)
    if(id < 0) {
      throw new GLException("uniform " + name + " not found")
    }
    id
  }

}

