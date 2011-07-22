package tuner

import scala.swing._
import scala.swing.KeyStroke._
import scala.swing.event._

import java.io.File

import tuner.gui.WindowMenu
import tuner.gui.ProjectChooser
import tuner.gui.ProjectInfoWindow
import tuner.gui.ProjectViewer
import tuner.gui.ResponseSelector
import tuner.gui.SamplingProgressBar
import tuner.project._

object Tuner extends SimpleSwingApplication {

  override def main(args:Array[String]) = {
    // Set up the menu bar for a mac
    //System.setProperty("apple.laf.useScreenMenuBar", "true")
    System.setProperty("apple.awt.showGrowBox", "true")
    System.setProperty("com.apple.mrj.application.growbox.intrudes", "false")
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Tuner")
    super.main(args)
  }

  var openWindows:Set[tuner.gui.Window] = Set()

  reactions += {
    case WindowClosed(tw:tuner.gui.Window) => 
      //println(tw + " closing")
      openWindows -= tw
      WindowMenu.updateWindows
      //println(openWindows)
      //maybeShowProjectWindow
      if(openWindows.isEmpty) ProjectChooser.open
  }

  //def top = ProjectChooser
  def top = { 
    //openProject(new Project(Some("/Users/tom/Projects/tuner/test_data/test_proj/")))
    //openProject(new Project(Some("/Users/tom/Projects/tuner/test_data/ahmed/")))
    ProjectChooser
  }

  def startNewProject = {
    println("Starting new project")
    val window = new ProjectInfoWindow
    window.open
  }

  def openProject() : Unit = {
    val fc = new FileChooser {
      title = "Select Project"
      fileSelectionMode = FileChooser.SelectionMode.DirectoriesOnly
    }
    fc.showOpenDialog(null) match {
      case FileChooser.Result.Approve => openProject(fc.selectedFile)
      case _ =>
    }
  }

  def openProject(proj:Project) : Unit = {
    println("opening project")
    proj match {
      case p:Saved => Config.recentProjects += p.path
      case _       =>
    }

    proj match {
      case nr:NewResponses =>
        val respWindow = new ResponseSelector(nr)
        ProjectChooser.close
        respWindow.open
      case ip:InProgress =>
        val waitWindow = new SamplingProgressBar(ip)
        ProjectChooser.close
        waitWindow.open
      case v:Viewable => 
        val projWindow = new ProjectViewer(v)
        ProjectChooser.close
        projWindow.open
      case _ => 
    }

    //maybeShowProjectWindow
  }

  def openProject(file:File) : Unit = {
    openProject(Project.fromFile(file.getAbsolutePath))
  }

  def saveCurrent : Unit = {
  }

  def saveCurrentAs : Unit = {
  }

  def listenTo(tunerWin:tuner.gui.Window) : Unit = {
    //println("listening to " + tunerWin)
    openWindows += tunerWin
    //println(openWindows)
    WindowMenu.updateWindows
    super.listenTo(tunerWin)
    //maybeShowProjectWindow
  }

  /*
  private def maybeShowProjectWindow = {
    // See if we need to show the project chooser
    if(openWindows.isEmpty)
      ProjectChooser.open
    else
      ProjectChooser.close
  }
  */

}

