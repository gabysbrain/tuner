package tuner

import scala.swing._
import scala.swing.event._

import tuner.gui.ProjectChooser
import tuner.gui.ProjectInfoWindow

object Tuner extends SimpleSwingApplication {

  override def main(args:Array[String]) = {
    // Set up the menu bar for a mac
    System.setProperty("apple.laf.useScreenMenuBar", "true")
    System.setProperty("apple.awt.showGrowBox", "true")
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Tuner")
    super.main(args)
  }

  def top = ProjectChooser

  def startNewProject = {
    println("Starting new project")
    val projWindow = new ProjectInfoWindow
    projWindow.visible = true
  }
}

