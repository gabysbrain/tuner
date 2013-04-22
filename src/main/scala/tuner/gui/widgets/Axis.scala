package tuner.gui.widgets

import javax.media.opengl.{GL,GL2,DebugGL2,GL2GL3,GL2ES1}
import com.jogamp.opengl.util.awt.TextRenderer

import tuner.Config
import tuner.geom.Rectangle
import tuner.gui.P5Panel
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

class Axis(placement:Axis.Placement) {
  
  import Axis._

  def draw(applet:P5Panel, x:Float, y:Float, w:Float, h:Float, 
           field:String, ticks:List[Float]) : Unit = {
    // set up all the colors and such
    applet.fill(Config.lineColor)
    applet.stroke(Config.lineColor)
    applet.strokeWeight(1)

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

  def draw(gl2:GL2, textRenderer:TextRenderer,
           x:Float, y:Float, w:Float, h:Float, 
           screenW:Int, screenH:Int,
           field:String, ticks:List[Float]) : Unit = {

    // Figure out what size to make the text and axes
    val axisOffset = Config.axisTickSize + Config.axisLabelSpace
    val labelSize = Config.smallFontSize
    val labelOffset = labelSize + Config.axisLabelSpace
    val t12 = System.currentTimeMillis
    placement match {
      case VerticalLeft =>
        val tickBox = Rectangle((x+w-Config.axisTickSize,y), (x+w, y+h))
        val labelBox = Rectangle((x,y), (x+labelSize,y+h))
        val textBox = Rectangle((x+labelOffset,y), (x+w-axisOffset,y+h)) 
        drawTicksVert(gl2, textRenderer, tickBox, textBox, 
                           screenW, screenH, ticks)
        drawLabelVert(gl2, textRenderer, labelBox, field, screenW, screenH)
      case VerticalRight =>
        val tickBox = Rectangle((x,y), (x+Config.axisTickSize, y+h))
        val labelBox = Rectangle((x+w-labelSize,y), (x+w,y+h))
        val textBox = Rectangle((x+axisOffset,y), (x+w-labelOffset,y+h))
        drawTicksVert(gl2, textRenderer, tickBox, textBox, 
                           screenW, screenH, ticks)
        drawLabelVert(gl2, textRenderer, labelBox, field, screenW, screenH)
      case HorizontalTop =>
        val tickBox = Rectangle((x,y+h-Config.axisTickSize), (x+w, y+h))
        val labelBox = Rectangle((x,y), (x+w,y+labelSize))
        val textBox = Rectangle((x,y+labelOffset), (x+w,y+h-axisOffset))
        drawTicksHoriz(gl2, textRenderer, tickBox, textBox, 
                            screenW, screenH, ticks)
        drawLabelHoriz(textRenderer, labelBox, field, screenW, screenH)
      case HorizontalBottom =>
        val tickBox = Rectangle((x,y), (x+w, y+Config.axisTickSize))
        val labelBox = Rectangle((x,y+h-labelSize), (x+w,y+h))
        val textBox = Rectangle((x,y+axisOffset), (x+w,y+h-labelOffset))
        drawTicksHoriz(gl2, textRenderer, tickBox, textBox, 
                            screenW, screenH, ticks)
        drawLabelHoriz(textRenderer, labelBox, field, screenW, screenH)
    }
    //println("int axis draw: " + (System.currentTimeMillis-t12))

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

  private def drawTicksVert(gl2:GL2, textRenderer:TextRenderer,
                            tickBox:Rectangle, textBox:Rectangle, 
                            screenW:Int, screenH:Int,
                            ticks:Seq[Float]) = {
    ticks.foreach {tick =>
      val yy = P5Panel.map(tick, ticks.head, ticks.last, 
                                 tickBox.maxY, tickBox.minY).toInt
      val gx1 = P5Panel.map(tickBox.minX, 0, screenW, -1, 1)
      val gx2 = P5Panel.map(tickBox.maxX, 0, screenW, -1, 1)
      val gyy = P5Panel.map(yy, screenH, 0, -1, 1)
      gl2.glColor3f(1f, 1f, 1f)
      gl2.glBegin(GL.GL_LINES)
        gl2.glVertex2f(gx1, gyy)
        gl2.glVertex2f(gx2, gyy)
      gl2.glEnd

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
    //println("vert tick draw: " + (System.currentTimeMillis-t13))
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

  private def drawTicksHoriz(gl2:GL2, textRenderer:TextRenderer,
                             tickBox:Rectangle, textBox:Rectangle, 
                             screenW:Int, screenH:Int,
                             ticks:Seq[Float]) = {
    ticks.foreach {tick =>
      val xx = P5Panel.map(tick, ticks.head, ticks.last, 
                                 tickBox.minX, tickBox.maxX).toInt
      //g.drawLine(xx, tickBox.minY.toInt, xx, tickBox.maxY.toInt)
      val gxx = P5Panel.map(xx, 0, screenW, -1, 1)
      val gy1 = P5Panel.map(tickBox.minY, screenH, 0, -1, 1)
      val gy2 = P5Panel.map(tickBox.maxY, screenH, 0, -1, 1)
      gl2.glColor3f(1f, 1f, 1f)
      gl2.glBegin(GL.GL_LINES)
        gl2.glVertex2f(gxx, gy1)
        gl2.glVertex2f(gxx, gy2)
      gl2.glEnd

      val (h, v) = if(tick == ticks.head) {
        (TextAlign.Left, TextAlign.Bottom)
      } else if(tick == ticks.last) {
        (TextAlign.Right, TextAlign.Bottom)
      } else {
        (TextAlign.Center, TextAlign.Bottom)
      }

      val txt = P5Panel.nfs(tick, Config.axisTickDigits._1, 
                                  Config.axisTickDigits._2)
      FontLib.drawVString(gl2, textRenderer, txt, 
                          xx, textBox.minY.toInt, 
                          h, v, 
                          screenW, screenH)
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

}

