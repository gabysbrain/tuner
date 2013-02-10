package tuner

import scala.swing._
import scala.swing.Dialog
import scala.swing.FileChooser
import scala.swing.KeyStroke._
import scala.swing.event._

import javax.swing.UIManager

import java.io.File

import tuner.error.MissingJriException
import tuner.error.ProjectLoadException
import tuner.gui.NewProjectWindow
import tuner.gui.ProjectChooser
import tuner.gui.ProjectViewer
import tuner.gui.ResponseSelector
import tuner.gui.SamplingProgressBar
import tuner.gui.WindowMenu
import tuner.gui.R.InstallPackageDialog
import tuner.gui.R.RNotInstalledDialog
import tuner.project._

/**
 * The entry application object for Tuner
 */
object Tuner extends SimpleSwingApplication {

  override def main(args:Array[String]) = {
    // Set up the menu bar for a mac
    System.setProperty("apple.laf.useScreenMenuBar", "true")
    System.setProperty("apple.awt.showGrowBox", "true")
    System.setProperty("com.apple.mrj.application.growbox.intrudes", "false")
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Tuner")

    //UIManager.setLookAndFeel(
      //UIManager.getSystemLookAndFeelClassName())
    super.main(args)
  }

  var openWindows:Set[tuner.gui.Window] = Set()

  reactions += {
    case WindowClosed(tw:tuner.gui.Window) => 
      openWindows -= tw
      WindowMenu.updateWindows
      deafTo(tw)
      // If there are no more open windows open the project chooser again
      if(openWindows.isEmpty) ProjectChooser.open
  }

  def top = { 
    // Make sure R and rJava are installed otherwise all bets are off
    if(!Rapp.pathOk) {
      RNotInstalledDialog
    } else if(!Rapp.jriOk) {
        new InstallPackageDialog(List("rJava")) {
          val installPackage = Rapp.installPackage(_)
        }
    } else {
      val missingPackages = R.missingPackages
      if(missingPackages.isEmpty) {
        //openProject(Project.fromFile("/Users/tom/Projects/tuner.jogl/test_data/britta_proj"))
        ProjectChooser
      } else {
        new InstallPackageDialog(missingPackages) {
          val installPackage = R.installPackage(_)
        }
      }
    }
  }

  def startNewProject = {
    println("Starting new project")
    val window = new NewProjectWindow
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
    try {
      openProject(Project.fromFile(file.getAbsolutePath))
    } catch {
      case ple:ProjectLoadException =>
        val msg = "Could not load project at %s (error: %s)".format(file.getAbsolutePath, ple.msg)
        Dialog.showMessage(message=msg, messageType=Dialog.Message.Error)
    }
  }

  def saveProjectAs(project:Project) : Unit = {
    println("here")
    val fc = new FileChooser {
      title = "Select Save Path"
      fileSelectionMode = FileChooser.SelectionMode.DirectoriesOnly
    }
    fc.showOpenDialog(null) match {
      case FileChooser.Result.Approve =>
        val path = fc.selectedFile.getAbsolutePath
        project.save(path)
      case _ =>
    }
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

