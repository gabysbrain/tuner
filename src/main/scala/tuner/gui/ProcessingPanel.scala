package tuner.gui

import java.awt.Dimension
import javax.swing.JInternalFrame

import processing.core.PApplet

import scala.swing.BorderPanel
import scala.swing.Component

abstract class ProcessingPanel(width:Int, height:Int) extends BorderPanel {
  
  val applet = {
    // need to rename these to prevent confusion with P5's 
    // setup and draw functions
    def _setup = setup
    def _draw = draw
    new PApplet {
      override def setup = {
        size(width, height)
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
    peer.setSize(width, height)
    peer.setPreferredSize(new Dimension(width, height))
    peer.setMaximumSize(new Dimension(width, height))
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
}

