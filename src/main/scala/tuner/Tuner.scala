package tuner

import scala.swing._
import scala.swing.event._

import java.io.File

import tuner.gui.ProjectChooser
import tuner.gui.ProjectInfoWindow
import tuner.gui.ProjectViewer
import tuner.gui.SamplingProgressBar

object Tuner extends SimpleSwingApplication {

  override def main(args:Array[String]) = {
    // Set up the menu bar for a mac
    System.setProperty("apple.laf.useScreenMenuBar", "true")
    System.setProperty("apple.awt.showGrowBox", "true")
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Tuner")
    super.main(args)
  }

  //def top = ProjectChooser
  def top = { 
    openProject(new Project(Some("/Users/tom/Projects/tuner/test_data/test_proj/")))
    ProjectChooser
  }

  def startNewProject = {
    println("Starting new project")
    val window = new ProjectInfoWindow(new Project)
    window.visible = true
  }

  def openProject(proj:Project) : Unit = {
    println("opening project")
    proj.path.foreach {
      Config.recentProjects += _
    }

    proj.status match {
      case Project.Ok =>
        val projWindow = new ProjectViewer(proj)
        projWindow.visible = true
      case Project.RunningSamples(_,_) | Project.BuildingGp =>
        val waitWindow = new SamplingProgressBar(ProjectChooser, proj)
        waitWindow.visible = true
      case _ =>
    }
  }

  def openProject(file:File) : Unit = {
    openProject(Project.fromFile(file.getAbsolutePath))
  }

}

