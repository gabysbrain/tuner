package tuner.gui

import scala.actors.Actor
import scala.actors.Actor._
import scala.swing.BoxPanel
import scala.swing.Button
import scala.swing.CheckBox
import scala.swing.Label
import scala.swing.Orientation
import scala.swing.ProgressBar
import scala.swing.event.ButtonClicked

import tuner.project.InProgress
import tuner.project.Project
import tuner.project.Saved

class SamplingProgressBar(project:InProgress) extends Window(project) {

  //modal = true
  //width = 800
  //height = 75

  val progressBar = new ProgressBar {
    min = 0
  }
  val alwaysBackgroundCheckbox = new CheckBox("Always Background") {
    selected = project.buildInBackground
  }
  val backgroundButton = new Button("Background")
  val stopButton = new Button("Stop")
  val progressLabel = new Label {
    text = "   "
  }

  listenTo(backgroundButton)
  listenTo(stopButton)

  contents = new BoxPanel(Orientation.Horizontal) {
    contents += new BoxPanel(Orientation.Vertical) {
      contents += progressBar
      contents += progressLabel
    }

    contents += alwaysBackgroundCheckbox
    contents += backgroundButton
    contents += stopButton
  }

  reactions += {
    case ButtonClicked(`backgroundButton`) =>
      project.buildInBackground = alwaysBackgroundCheckbox.selected
      project match {
        case s:Saved => s.save
        case _       =>
      }
      close
    case ButtonClicked(`stopButton`) => 
      project.stop
      close
  }

  project.start

  updateProgress

  private var runScanner = true
  private val scanner:Actor = actor {
    while(runScanner) {
      Thread.sleep(500)
      updateProgress
      runScanner = !project.finished
    }
    openNextStage
  }

  def updateProgress = {
    progressLabel.text = project.asInstanceOf[Project].statusString

    val (cur, max) = project.runStatus

    if(max > 0) {
      progressBar.indeterminate = false
      progressBar.max = max
      progressBar.value = cur
    } else {
      progressBar.indeterminate = true
    }
    this.pack
  }

}

