package tuner.gui.widgets

import com.jogamp.common.nio.Buffers
import javax.media.opengl.{GL,GL2,DebugGL2,GL2GL3,GL2ES1}
import javax.media.opengl.fixedfunc.GLPointerFunc
import com.jogamp.opengl.util.awt.TextRenderer
import java.awt.Graphics2D

import scala.collection.mutable.ArrayBuffer

import tuner.Config
import tuner.geom.Rectangle
import tuner.gui.P5Panel
import tuner.gui.opengl.Glsl
import tuner.gui.util.FontLib
import tuner.gui.util.TextAlign
import tuner.util.AxisTicks

//import java.awt.Graphics2D

object Axis {
  sealed trait Placement 
  // Vertical, text on left side
  case object VerticalLeft extends Placement
  // Vertical, text on right side
  case object VerticalRight extends Placement
  // Horizontal, text on top
  case object HorizontalTop extends Placement
  // Horizontal, text on bottom
  case object HorizontalBottom extends Placement
}

// Need this accross multiple calls
class OpenGLAxis(val gl2:GL2, tr:TextRenderer) { 
  // We add and clear this
  private val points = new ArrayBuffer[Float]

  val tickVBO = Array(-1)
  var shader:Option[Glsl] = None

  def add(x:Float, y:Float) = points.append(x, y)

  def begin = {
    val es2 = new javax.media.opengl.DebugGL2ES2(gl2.getGL2ES2)

    if(tickVBO(0) == -1) {
      es2.glGenBuffers(1, tickVBO, 0)
    }
    if(shader == None) {
      shader = Some(Glsl.fromResource(es2, "/shaders/ticks.vert.glsl", 
                                           "/shaders/ticks.frag.glsl"))
    }

    points.clear
  }

  def end(screenW:Int, screenH:Int) = {
    val es2 = new javax.media.opengl.DebugGL2ES2(gl2.getGL2ES2)

    // Load the data into our buffer
    es2.glBindBuffer(GL.GL_ARRAY_BUFFER, tickVBO(0))
    val buf = Buffers.newDirectFloatBuffer(points.toArray)
    buf.rewind
    es2.glBufferData(GL.GL_ARRAY_BUFFER, points.length*Buffers.SIZEOF_FLOAT, 
                     buf, GL.GL_DYNAMIC_DRAW)

    // Do the draw
    es2.glUseProgram(shader.get.programId)
    es2.glBindBuffer(GL.GL_ARRAY_BUFFER, tickVBO(0))
    es2.glVertexAttribPointer(shader.get.attribId("pos"), 2, GL.GL_FLOAT, false, 2*Buffers.SIZEOF_FLOAT, 0)
    es2.glEnableVertexAttribArray(shader.get.attribId("pos"))
    es2.glDrawArrays(GL.GL_LINES, 0, points.length / 2)

    // Cleanup
    es2.glUseProgram(0)
    es2.glBindBuffer(GL.GL_ARRAY_BUFFER, 0)
  }

  def dispose = {
    if(tickVBO(0) != -1) {
      gl2.glDeleteBuffers(1, tickVBO, 0)
      tickVBO(0) = -1
      shader = None
    }
  }
}

class Axis(placement:Axis.Placement) {
  
  import Axis._

  var minVal:Float = 0f
  var maxVal:Float = 1f
  var size:Float = 0f
  var ticks = List(minVal, maxVal)

  def draw(applet:P5Panel, x:Float, y:Float, w:Float, h:Float, 
           field:String, low:Float, high:Float) : Unit = {
    // set up all the colors and such
    applet.fill(Config.lineColor)
    applet.stroke(Config.lineColor)
    applet.strokeWeight(1)
    updateTicks(low, high, h)

    // Figure out what size to make the text and axes
    val axisOffset = Config.axisTickSize + Config.axisLabelSpace
    val labelSize = Config.smallFontSize
    val labelOffset = labelSize + Config.axisLabelSpace
    placement match {
      case VerticalLeft =>
        val tickBox = Rectangle((x+w-Config.axisTickSize,y), (x+w, y+h))
        val labelBox = Rectangle((x,y), (x+labelSize,y+h))
        val textBox = Rectangle((x+labelOffset,y), (x+w-axisOffset,y+h)) 
        drawTicksVert(applet, tickBox, textBox, ticks)
        drawLabelVert(applet, labelBox, field)
      case VerticalRight =>
        val tickBox = Rectangle((x,y), (x+Config.axisTickSize, y+h))
        val labelBox = Rectangle((x+w-labelSize,y), (x+w,y+h))
        val textBox = Rectangle((x+axisOffset,y), (x+w-labelOffset,y+h))
        drawTicksVert(applet, tickBox, textBox, ticks)
        drawLabelVert(applet, labelBox, field)
      case HorizontalTop =>
        val tickBox = Rectangle((x,y+h-Config.axisTickSize), (x+w, y+h))
        val labelBox = Rectangle((x,y), (x+w,y+labelSize))
        val textBox = Rectangle((x,y+labelOffset), (x+w,y+h-axisOffset))
        drawTicksHoriz(applet, tickBox, textBox, ticks)
        drawLabelHoriz(applet, labelBox, field)
      case HorizontalBottom =>
        val tickBox = Rectangle((x,y), (x+w, y+Config.axisTickSize))
        val labelBox = Rectangle((x,y+h-labelSize), (x+w,y+h))
        val textBox = Rectangle((x,y+axisOffset), (x+w,y+h-labelOffset))
        drawTicksHoriz(applet, tickBox, textBox, ticks)
        drawLabelHoriz(applet, labelBox, field)
    }

  }

  def draw(glAxis:OpenGLAxis, textRenderer:TextRenderer,
           x:Float, y:Float, w:Float, h:Float, 
           screenW:Int, screenH:Int,
           field:String, low:Float, high:Float) : Unit = {

    // Figure out what size to make the text and axes
    val axisOffset = Config.axisTickSize + Config.axisLabelSpace
    val labelSize = Config.smallFontSize
    val labelOffset = labelSize + Config.axisLabelSpace
    updateTicks(low, high, h)

    placement match {
      case VerticalLeft =>
        val tickBox = Rectangle((x+w-Config.axisTickSize,y), (x+w, y+h))
        val labelBox = Rectangle((x,y), (x+labelSize,y+h))
        val textBox = Rectangle((x+labelOffset,y), (x+w-axisOffset,y+h)) 
        drawTicksVert(glAxis, textRenderer, tickBox, textBox, 
                              screenW, screenH, ticks)
        drawLabelVert(glAxis.gl2, textRenderer, labelBox, field, 
                                  screenW, screenH)
      case VerticalRight =>
        val tickBox = Rectangle((x,y), (x+Config.axisTickSize, y+h))
        val labelBox = Rectangle((x+w-labelSize,y), (x+w,y+h))
        val textBox = Rectangle((x+axisOffset,y), (x+w-labelOffset,y+h))
        drawTicksVert(glAxis, textRenderer, tickBox, textBox, 
                              screenW, screenH, ticks)
        drawLabelVert(glAxis.gl2, textRenderer, labelBox, field, 
                                  screenW, screenH)
      case HorizontalTop =>
        val tickBox = Rectangle((x,y+h-Config.axisTickSize), (x+w, y+h))
        val labelBox = Rectangle((x,y), (x+w,y+labelSize))
        val textBox = Rectangle((x,y+labelOffset), (x+w,y+h-axisOffset))
        drawTicksHoriz(glAxis, textRenderer, tickBox, textBox, 
                               screenW, screenH, ticks)
        drawLabelHoriz(textRenderer, labelBox, field, screenW, screenH)
      case HorizontalBottom =>
        val tickBox = Rectangle((x,y), (x+w, y+Config.axisTickSize))
        val labelBox = Rectangle((x,y+h-labelSize), (x+w,y+h))
        val textBox = Rectangle((x,y+axisOffset), (x+w,y+h-labelOffset))
        drawTicksHoriz(glAxis, textRenderer, tickBox, textBox, 
                               screenW, screenH, ticks)
        drawLabelHoriz(textRenderer, labelBox, field, screenW, screenH)
    }
  }

  def draw(j2d:Graphics2D, x:Float, y:Float, w:Float, h:Float, 
           screenW:Int, screenH:Int,
           field:String, low:Float, high:Float) : Unit = {

    // Figure out what size to make the text and axes
    val axisOffset = Config.axisTickSize + Config.axisLabelSpace
    val labelSize = Config.smallFontSize
    val labelOffset = labelSize + Config.axisLabelSpace
    updateTicks(low, high, h)

    placement match {
      case VerticalLeft =>
        val tickBox = Rectangle((x+w-Config.axisTickSize,y), (x+w, y+h))
        val labelBox = Rectangle((x,y), (x+labelSize,y+h))
        val textBox = Rectangle((x+labelOffset,y), (x+w-axisOffset,y+h)) 
        drawTicksVert(j2d, tickBox, textBox, screenW, screenH, ticks)
        drawLabelVert(j2d, labelBox, field, screenW, screenH)
      case VerticalRight =>
        val tickBox = Rectangle((x,y), (x+Config.axisTickSize, y+h))
        val labelBox = Rectangle((x+w-labelSize,y), (x+w,y+h))
        val textBox = Rectangle((x+axisOffset,y), (x+w-labelOffset,y+h))
        drawTicksVert(j2d, tickBox, textBox, screenW, screenH, ticks)
        drawLabelVert(j2d, labelBox, field, screenW, screenH)
      case HorizontalTop =>
        val tickBox = Rectangle((x,y+h-Config.axisTickSize), (x+w, y+h))
        val labelBox = Rectangle((x,y), (x+w,y+labelSize))
        val textBox = Rectangle((x,y+labelOffset), (x+w,y+h-axisOffset))
        drawTicksHoriz(j2d, tickBox, textBox, screenW, screenH, ticks)
        drawLabelHoriz(j2d, labelBox, field, screenW, screenH)
      case HorizontalBottom =>
        val tickBox = Rectangle((x,y), (x+w, y+Config.axisTickSize))
        val labelBox = Rectangle((x,y+h-labelSize), (x+w,y+h))
        val textBox = Rectangle((x,y+axisOffset), (x+w,y+h-labelOffset))
        drawTicksHoriz(j2d, tickBox, textBox, screenW, screenH, ticks)
        drawLabelHoriz(j2d, labelBox, field, screenW, screenH)
    }
  }

  // The calls to R tick calculator is slow, so cache results
  private def updateTicks(low:Float, high:Float, height:Float) = {
    if(low != minVal || high != maxVal || height != size) {
      minVal = low
      maxVal = high
      size = height
      ticks = AxisTicks.ticks(low, high, height, Config.smallFontSize)
    }
    ticks
  }

  private def drawTicksVert(applet:P5Panel, tickBox:Rectangle, 
                            textBox:Rectangle, ticks:Seq[Float]) = {
    ticks.foreach {tick =>
      val yy = P5Panel.map(tick, ticks.head, ticks.last, 
                                 tickBox.maxY, tickBox.minY)
      applet.line(tickBox.minX, yy, tickBox.maxX, yy)

      // Also draw the label
      if(tick == ticks.head) {
        applet.textAlign(P5Panel.TextHAlign.Center, P5Panel.TextVAlign.Bottom)
      } else if(tick == ticks.last) {
        applet.textAlign(P5Panel.TextHAlign.Center, P5Panel.TextVAlign.Top)
      } else {
        applet.textAlign(P5Panel.TextHAlign.Center, P5Panel.TextVAlign.Center)
      }

      applet.text(P5Panel.nfs(tick, Config.axisTickDigits._1, 
                                    Config.axisTickDigits._2), 
                  textBox.center._1, yy)
    }
  }

  private def drawTicksVert(glAxis:OpenGLAxis, textRenderer:TextRenderer,
                            tickBox:Rectangle, textBox:Rectangle, 
                            screenW:Int, screenH:Int,
                            ticks:Seq[Float]) = {
    ticks.foreach {tick =>
      val yy = P5Panel.map(tick, ticks.head, ticks.last, 
                                 tickBox.maxY, tickBox.minY).toInt
      val gx1 = P5Panel.map(tickBox.minX, 0, screenW, -1, 1)
      val gx2 = P5Panel.map(tickBox.maxX, 0, screenW, -1, 1)
      val gyy = P5Panel.map(yy, screenH, 0, -1, 1)
      glAxis.add(gx1, gyy)
      glAxis.add(gx2, gyy)

      // Also draw the label
      val (h, v) = if(tick == ticks.head) {
        (TextAlign.Right, TextAlign.Bottom)
      } else if(tick == ticks.last) {
        (TextAlign.Right, TextAlign.Top)
      } else {
        (TextAlign.Right, TextAlign.Middle)
      }

      val txt = P5Panel.nfs(tick, Config.axisTickDigits._1,
                                  Config.axisTickDigits._2)
      FontLib.drawString(textRenderer, txt, 
                         textBox.maxX.toInt, yy, 
                         h, v, 
                         screenW, screenH)
    }
  }

  private def drawTicksVert(j2d:Graphics2D,
                            tickBox:Rectangle, textBox:Rectangle, 
                            screenW:Int, screenH:Int,
                            ticks:Seq[Float]) = {
    ticks.foreach {tick =>
      val yy = P5Panel.map(tick, ticks.head, ticks.last, 
                                 tickBox.maxY, tickBox.minY).toInt
      j2d.drawLine(tickBox.minX.toInt, yy, tickBox.maxX.toInt, yy)

      // Also draw the label
      val (h, v) = if(tick == ticks.head) {
        (TextAlign.Right, TextAlign.Bottom)
      } else if(tick == ticks.last) {
        (TextAlign.Right, TextAlign.Top)
      } else {
        (TextAlign.Right, TextAlign.Middle)
      }

      val txt = P5Panel.nfs(tick, Config.axisTickDigits._1,
                                  Config.axisTickDigits._2)
      FontLib.drawString(j2d, txt, 
                         textBox.maxX.toInt, yy, 
                         h, v)
    }
  }

  private def drawTicksHoriz(applet:P5Panel, tickBox:Rectangle, 
                             textBox:Rectangle, ticks:Seq[Float]) = {
    ticks.foreach {tick =>
      val xx = P5Panel.map(tick, ticks.head, ticks.last, 
                                 tickBox.minX, tickBox.maxX)
      applet.line(xx, tickBox.minY, xx, tickBox.maxY)

      // Also draw the label rotated vertically
      applet.pushMatrix
      applet.translate(xx, textBox.center._2)
      applet.rotate(-P5Panel.HalfPi)

      if(tick == ticks.head) {
        applet.textAlign(P5Panel.TextHAlign.Center, P5Panel.TextVAlign.Top)
      } else if(tick == ticks.last) {
        applet.textAlign(P5Panel.TextHAlign.Center, P5Panel.TextVAlign.Bottom)
      } else {
        applet.textAlign(P5Panel.TextHAlign.Center, P5Panel.TextVAlign.Center)
      }

      applet.text(P5Panel.nfs(tick, Config.axisTickDigits._1, 
                                    Config.axisTickDigits._2), 
                  0, 0)
      
      applet.popMatrix
    }
  }

  private def drawTicksHoriz(glAxis:OpenGLAxis, textRenderer:TextRenderer,
                             tickBox:Rectangle, textBox:Rectangle, 
                             screenW:Int, screenH:Int,
                             ticks:Seq[Float]) = {
    ticks.foreach {tick =>
      val xx = P5Panel.map(tick, ticks.head, ticks.last, 
                                 tickBox.minX, tickBox.maxX).toInt
      val gxx = P5Panel.map(xx, 0, screenW, -1, 1)
      val gy1 = P5Panel.map(tickBox.minY, screenH, 0, -1, 1)
      val gy2 = P5Panel.map(tickBox.maxY, screenH, 0, -1, 1)
      glAxis.add(gxx, gy1)
      glAxis.add(gxx, gy2)
      
      val (h, v) = if(tick == ticks.head) {
        (TextAlign.Left, TextAlign.Bottom)
      } else if(tick == ticks.last) {
        (TextAlign.Right, TextAlign.Bottom)
      } else {
        (TextAlign.Center, TextAlign.Bottom)
      }

      val txt = P5Panel.nfs(tick, Config.axisTickDigits._1, 
                                  Config.axisTickDigits._2)
      FontLib.drawVString(glAxis.gl2, textRenderer, txt, 
                          xx, textBox.minY.toInt, 
                          h, v, 
                          screenW, screenH)
    }
  }

  private def drawTicksHoriz(j2d:Graphics2D,
                             tickBox:Rectangle, textBox:Rectangle, 
                             screenW:Int, screenH:Int,
                             ticks:Seq[Float]) = {
    ticks.foreach {tick =>
      val xx = P5Panel.map(tick, ticks.head, ticks.last, 
                                 tickBox.minX, tickBox.maxX).toInt
      j2d.drawLine(xx, tickBox.minY.toInt, xx, tickBox.maxY.toInt)

      val (h, v) = if(tick == ticks.head) {
        (TextAlign.Left, TextAlign.Bottom)
      } else if(tick == ticks.last) {
        (TextAlign.Right, TextAlign.Bottom)
      } else {
        (TextAlign.Center, TextAlign.Bottom)
      }

      val txt = P5Panel.nfs(tick, Config.axisTickDigits._1, 
                                  Config.axisTickDigits._2)
      FontLib.drawVString(j2d, txt, 
                          xx, textBox.minY.toInt, 
                          h, v)
    }
  }

  private def drawLabelVert(applet:P5Panel, labelBox:Rectangle,
                            label:String) = {
    
    val centerPt = labelBox.center

    applet.pushMatrix
    applet.translate(centerPt._1, centerPt._2)
    applet.rotate(-P5Panel.HalfPi)

    //applet.textMode(P5Panel.TextMode.Model)
    applet.textFont(Config.fontPath, Config.smallFontSize)
    applet.textAlign(P5Panel.TextHAlign.Center, P5Panel.TextVAlign.Center)
    applet.text(label, 0, 0)

    applet.popMatrix
  }

  private def drawLabelVert(gl2:GL2, textRenderer:TextRenderer,
                            labelBox:Rectangle, label:String,
                            screenW:Int, screenH:Int) = {
    
    val centerPt = labelBox.center
    FontLib.drawVString(gl2, textRenderer, label, 
                        centerPt._1.toInt, centerPt._2.toInt, 
                        TextAlign.Center, TextAlign.Middle,
                        screenW, screenH)
  }

  private def drawLabelVert(j2d:Graphics2D,
                            labelBox:Rectangle, label:String,
                            screenW:Int, screenH:Int) = {
    
    val centerPt = labelBox.center
    FontLib.drawVString(j2d, label, 
                        centerPt._1.toInt, centerPt._2.toInt, 
                        TextAlign.Center, TextAlign.Middle)
  }

  private def drawLabelHoriz(applet:P5Panel, labelBox:Rectangle, 
                             label:String) = {

    //applet.textMode(P5Panel.TextMode.Model)
    applet.textFont(Config.fontPath, Config.smallFontSize)
    applet.textAlign(P5Panel.TextHAlign.Center, P5Panel.TextVAlign.Center)
    val centerPt = labelBox.center
    applet.text(label, centerPt._1, centerPt._2)
  }

  private def drawLabelHoriz(textRenderer:TextRenderer, 
                             labelBox:Rectangle, label:String,
                             screenW:Int, screenH:Int) = {

    val centerPt = labelBox.center
    FontLib.drawString(textRenderer, label, 
                       centerPt._1.toInt, centerPt._2.toInt, 
                       TextAlign.Center, TextAlign.Middle,
                       screenW, screenH)
  }

  private def drawLabelHoriz(j2d:Graphics2D,
                             labelBox:Rectangle, label:String,
                             screenW:Int, screenH:Int) = {

    val centerPt = labelBox.center
    FontLib.drawString(j2d, label, 
                       centerPt._1.toInt, centerPt._2.toInt, 
                       TextAlign.Center, TextAlign.Middle)
  }

}

