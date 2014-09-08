package tuner.gui

import scala.swing.Frame

import tuner.Config
import tuner.Tuner
import tuner.project.Project

abstract class Window(val project:Project) extends Frame {

  // register this window with Tuner
  Tuner.listenTo(this)

  def toFront = {
    visible = true
    peer.toFront
  }

}
