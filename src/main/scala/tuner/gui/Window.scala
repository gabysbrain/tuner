package tuner.gui

import scala.swing.Frame

import tuner.Tuner
import tuner.project.Project

abstract class Window(val project:Project) extends Frame {

  // register this window with Tuner
  Tuner.listenTo(this)

  def openNextStage = {
    Tuner.openProject(project.next)
    close
  }

  override def closeOperation = {
    Tuner.deafTo(this)

    menuBar match {
      case mb:tuner.gui.MainMenu => WindowMenu.menus -= mb
      case _ =>
    }
    dispose
  }

  def toFront = {
    visible = true
    peer.toFront
  }

}

