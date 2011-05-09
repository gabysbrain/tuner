package tuner.gui

import java.awt.Dimension
import javax.swing.JInternalFrame

import processing.core.PApplet
import processing.core.PConstants

import scala.swing.BorderPanel
import scala.swing.Component

object P5Panel {
  sealed trait Renderer {def name : String}
  case object Java2D extends Renderer {val name = PConstants.JAVA2D}
  case object P2D extends Renderer {val name = PConstants.P2D}
  case object P3D extends Renderer {val name = PConstants.P3D}
  case object OpenGL extends Renderer {val name = PConstants.OPENGL}
  case object PDF extends Renderer {val name = PConstants.PDF}

  object Shape extends Enumeration {
    val TriangleStrip = Value(PConstants.TRIANGLE_STRIP)
  }

  object ColorSpace extends Enumeration {
    val RGB = Value(PConstants.RGB)
    val HSB = Value(PConstants.HSB)
  }

  type Color = Int

  def map(value:Float, low1:Float, high1:Float, low2:Float, high2:Float) =
    PApplet.map(value, low1, high1, low2, high2)

  def constrain(value:Float, low:Float, high:Float) = 
    PApplet.constrain(value, low, high)

  def norm(value:Float, low:Float, high:Float) = 
    PApplet.norm(value, low, high)
  
  def lerpColor(c1:Color, c2:Color, amt:Float, method:ColorSpace.Value) =
    PApplet.lerpColor(c1, c2, amt, method.id)
}

abstract class P5Panel (
    _width:Int, _height:Int, renderer:P5Panel.Renderer=P5Panel.Java2D) 
    extends BorderPanel {
  
  import P5Panel._

  val applet = {
    // need to rename these to prevent confusion with P5's 
    // setup and draw functions
    def _setup = setup
    def _draw = draw
    new PApplet {
      override def setup = {
        size(_width, _height, renderer.name)
        _setup
      }

      override def draw = {
        _draw
      }
    }
  }

  // From http://forum.processing.org/topic/papplet-in-jscrollpane
  // set up a lightweight container for P5 and strip the UI
  val sketchFrame = new Component {
    override lazy val peer = new JInternalFrame with SuperMixin
    val ui = peer.getUI.asInstanceOf[javax.swing.plaf.basic.BasicInternalFrameUI]
    peer.putClientProperty("titlePane", ui.getNorthPane)
    peer.putClientProperty("border", peer.getBorder)
    ui.setNorthPane(null)
    peer.setBorder(null)
    // Set up P5's container frame
    peer.add(applet)
    peer.setSize(_width, _height)
    peer.setPreferredSize(new Dimension(_width, _height))
    peer.setMaximumSize(new Dimension(_width, _height))
    peer.setResizable(false)
    peer.setVisible(true)
    peer.pack
  }

  layout(sketchFrame) = BorderPanel.Position.Center

  applet.init

  def setup = {}
  def draw

  def text(stringdata:String, x:Float, y:Float) = {
    applet.text(stringdata, x, y)
  }

  def height = applet.height
  def width = applet.width

  def pushMatrix = applet.pushMatrix
  def popMatrix = applet.popMatrix
  def translate(x:Float, y:Float) = applet.translate(x, y)

  def stroke(color:Color) = applet.stroke(color)
  def noStroke = applet.noStroke

  def fill(color:Color) = applet.fill(color)
  def noFill = applet.noFill

  def beginShape(shape:Shape.Value) = applet.beginShape(shape.id)
  def endShape = applet.endShape
  def vertex(x:Float, y:Float) = applet.vertex(x, y)

}

