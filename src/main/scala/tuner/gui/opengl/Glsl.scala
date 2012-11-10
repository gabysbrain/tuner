package tuner.gui.opengl

import com.jogamp.opengl.util.glsl.ShaderUtil
import javax.media.opengl.{GL,GL2,GL2ES2}
import javax.media.opengl.DebugGL2ES2
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
    new Glsl(gl, vertSource, Some(geomSource), fragSource, List())
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
           fragmentSource:String,
           bindings:List[(String,Int)]) {

  //val es2 = gl.getGL2ES2
  val es2 = new DebugGL2ES2(gl.getGL2ES2)

  val vertShaderId = 
    createShader(GL2ES2.GL_VERTEX_SHADER, vertexSource)
  val geomShaderId = geometrySource.map {gs => 
    createShader(GL2.GL_GEOMETRY_PROGRAM_NV, gs)
  }
  val fragShaderId = 
    createShader(GL2ES2.GL_FRAGMENT_SHADER, fragmentSource)
  val programId = {
    val progId = es2.glCreateProgram
    // Fix any bindings before linking
    bindings.foreach {case (nm,idx) =>
      println("binding " + nm + " to " + idx)
      es2.glBindAttribLocation(progId, idx, nm)
    }
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

  // Build up all the vertex attributes
  var attribIds = bindings.toMap

  def this(gl:GL, vertexSource:String, fragmentSource:String) = 
        this(gl, vertexSource, None, fragmentSource, List())

  protected def createShader(typeId:Int, source:String) = {
    val shaderId = es2.glCreateShader(typeId)
    es2.glShaderSource(shaderId, 1, Array(source), null)
    es2.glCompileShader(shaderId)
    val ok = ShaderUtil.isShaderStatusValid(es2, shaderId, GL2ES2.GL_COMPILE_STATUS)
    if(!ok) {
      throw new javax.media.opengl.GLException(
        source.split("\n").zipWithIndex.map {case (ln:String, no:Int) =>
          println((no+1) + ": " + ln)
        }.reduceLeft(_ + "\n" + _) + "\n\n" +
        ShaderUtil.getShaderInfoLog(es2, shaderId))
    }
    shaderId
  }

  def attribSize = {
    var tmp = Array(-1)
    es2.glGetProgramiv(programId, GL2ES2.GL_ACTIVE_ATTRIBUTES, tmp, 0)
    tmp(0)
  }

  def attrib(id:Int) = {
    var len = Array.fill(1024)(-1)
    var typ = Array.fill(1024)(-1)
    var sz = Array.fill(1024)(-1)
    var nm:Array[Byte] = Array.fill(1024)(0)
    es2.glGetActiveAttrib(programId, id, 1024, len, 0, sz, 0, typ, 0, nm, 0)
    new String(nm).trim
  }

  def attribId(name:String) : Int = try {
    attribIds(name)
  } catch {
    case e:NoSuchElementException =>
      // not found but cache this for later
      val eleId = es2.glGetAttribLocation(programId, name)
      if(eleId < 0) {
        throw new NoSuchElementException("attr " + name + " not in program")
      }
      attribIds = attribIds + (name -> eleId)
      eleId
  }

  def uniformId(name:String) : Int = {
    val id = es2.glGetUniformLocation(programId, name)
    if(id < 0) {
      throw new GLException("uniform " + name + " not found")
    }
    id
  }

}

