package tuner

import scala.swing._
import scala.swing.event._

import java.io.File

import tuner.gui.ProjectChooser
import tuner.gui.ProjectInfoWindow
import tuner.gui.ProjectViewer
import tuner.gui.SamplingProgressBar
import tuner.project._

object Tuner extends SimpleSwingApplication {

  override def main(args:Array[String]) = {
    // Set up the menu bar for a mac
    System.setProperty("apple.laf.useScreenMenuBar", "true")
    System.setProperty("apple.awt.showGrowBox", "true")
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Tuner")
    super.main(args)
  }

  var openProjects:Map[Project,Window] = Map()
  var newProjectWindow:Option[ProjectInfoWindow] = None

  //def top = ProjectChooser
  def top = { 
    //openProject(new Project(Some("/Users/tom/Projects/tuner/test_data/test_proj/")))
    //openProject(new Project(Some("/Users/tom/Projects/tuner/test_data/ahmed/")))
    ProjectChooser
  }

  def startNewProject = {
    println("Starting new project")
    val window = new ProjectInfoWindow
    newProjectWindow = Some(window)
    window.open
  }

  def openProject(proj:Project) : Unit = {
    println("opening project")
    proj match {
      case p:Saved => Config.recentProjects += p.path
      case _       =>
    }

    proj match {
      case v:Viewable => 
        val projWindow = new ProjectViewer(v)
        openProjects += (proj -> projWindow)
        ProjectChooser.close
        projWindow.open
      case ip:InProgress =>
        val waitWindow = new SamplingProgressBar(ip)
        openProjects += (proj -> waitWindow)
        ProjectChooser.close
        waitWindow.open
      case _ => 
    }
    /*
    proj.status match {
      case Project.Ok =>
        val projWindow = new ProjectViewer(proj)
        openProjects += (proj -> projWindow)
        ProjectChooser.close
        projWindow.open
      case Project.RunningSamples(_,_) =>
        proj.runSamples
        val waitWindow = new SamplingProgressBar(proj)
        openProjects += (proj -> waitWindow)
        ProjectChooser.close
        waitWindow.open
      case Project.BuildingGp =>
        val waitWindow = new SamplingProgressBar(proj)
        openProjects += (proj -> waitWindow)
        ProjectChooser.close
        waitWindow.open
      case _ =>
    }
    */
  }

  def openProject(file:File) : Unit = {
    openProject(Project.fromFile(file.getAbsolutePath))
  }

  def closeProject(proj:Project) : Unit = {
    openProjects.get(proj) match {
      case Some(window) =>
        openProjects -= proj
        window.close
        //window.dispose
      case None         => 
        println("project doesn't have a window open...")
    }
    // See if we need to show the project chooser
    if(openProjects.isEmpty)
      ProjectChooser.open
  }

  def reloadProject(proj:Project) = {
    closeProject(proj)
    proj match {
      case x:Saved => openProject(new File(x.path))
      case _ => openProject(proj)
    }
  }

}

