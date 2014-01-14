package tuner

import scala.swing._
import scala.swing.Dialog
import scala.swing.FileChooser
import scala.swing.KeyStroke._
import scala.swing.event._
import scala.io.Source

import scala.concurrent._
import ExecutionContext.Implicits.global

import javax.swing.UIManager

import java.io.File
import java.io.FileWriter

import tuner.error.ProjectLoadException
import tuner.gui.NewProjectWindow
import tuner.gui.ProjectChooser
import tuner.gui.ProjectViewer
import tuner.gui.ResponseSelector
import tuner.gui.SamplingProgressBar
import tuner.gui.WindowMenu
import tuner.project._

import net.liftweb.json._
import net.liftweb.json.Extraction._
import net.liftweb.json.JsonDSL._

case class TunerPrefs(
  var recentProjects:Set[String]
)

/**
 * The entry application object for Tuner
 */
object Tuner extends SimpleSwingApplication {

  // Serializers to get the json parser to work
  implicit val formats = net.liftweb.json.DefaultFormats

  val prefsPath = System.getProperty("user.home") + (OS.detect match {
    case OS.Mac  => "/Library/Preferences/at.ac.univie.cs.tuner.json"
    case OS.Win  => "\\" + System.getenv("APPDATA") + 
                    "\\UniVie Software\\Tuner\\prefs.json"
    case OS.Unix => "/.univie.tuner.json"
  })

  private def savePrefs(p:TunerPrefs) = {
    val outFile = new FileWriter(prefsPath)
    outFile.write(pretty(render(decompose(p))))
    outFile.close
  }

  val prefs = {
    try {
      val json = parse(Source.fromFile(prefsPath).mkString)
      json.extract[TunerPrefs]
    } catch {
      case ioe:java.io.FileNotFoundException =>
        // If there's no prefs file create a default one
        val newJson = new TunerPrefs(
          recentProjects = Set()
        )
        savePrefs(newJson)
        newJson
    }
  }

  override def main(args:Array[String]) = {
    // Set up the menu bar for a mac
    System.setProperty("apple.laf.useScreenMenuBar", "true")
    System.setProperty("apple.awt.showGrowBox", "true")
    System.setProperty("com.apple.mrj.application.growbox.intrudes", "false")
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Tuner")

    super.main(args)
  }

  var openWindows:Set[tuner.gui.Window] = Set()

  reactions += {
    case WindowClosed(tw:tuner.gui.Window) =>
      println("here 2")
      handleWindowClose(tw)
    case WindowClosing(tw:tuner.gui.Window) => 
      println("here closing")
      handleWindowClose(tw)
  }

  def top = { 
    ProjectChooser
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
      case p:Saved => prefs.recentProjects = prefs.recentProjects + p.path
                      savePrefs(prefs)
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
        future {ip.start}
      case v:Viewable => 
        val projWindow = new ProjectViewer(v)
        ProjectChooser.close
        projWindow.open
      case _ => 
    }

    //maybeShowProjectWindow
  }

  def openProject(proj:ProjectInfo) : Unit = {
    openProject(Project.fromFile(proj.path))
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
  }

  def deafTo(tunerWin:tuner.gui.Window) : Unit = {
    super.deafTo(tunerWin)

    openWindows -= tunerWin
    //maybeShowProjectWindow
  }

  protected def handleWindowClose(tw:tuner.gui.Window) = {
    openWindows -= tw
    WindowMenu.updateWindows
    deafTo(tw)

    if(!Config.testingMode) {
      // If a project is moving to another stage then we 
      // should automatically open that window
      (tw.project, tw.project.next) match {
        case (p:Viewable, pn:Viewable) =>
        case _ => Tuner.openProject(tw.project.next)
      }

      // If there are no more open windows open the project chooser again
      if(openWindows.isEmpty) ProjectChooser.open
    } else {
      System.exit(0)
    }
  }
}

