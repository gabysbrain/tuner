package tuner.gui

import scala.actors.Actor
import scala.actors.Actor._
import scala.swing.BoxPanel
import scala.swing.Button
import scala.swing.CheckBox
import scala.swing.Dialog
import scala.swing.Label
import scala.swing.Orientation
import scala.swing.ProgressBar
import scala.swing.Window

import tuner.Project

class SamplingProgressBar(owner:Window, project:Project) extends Dialog(owner) {

  modal = true
  //width = 800
  //height = 75

  val progressBar = new ProgressBar {
    min = 0
  }
  val alwaysBackgroundCheckbox = new CheckBox("Always Background")
  val backgroundButton = new Button("Background")
  val stopButton = new Button("Stop")
  val progressLabel = new Label {
    text = "   "
  }

  contents = new BoxPanel(Orientation.Horizontal) {
    contents += new BoxPanel(Orientation.Vertical) {
      contents += progressBar
      contents += progressLabel
    }

    contents += alwaysBackgroundCheckbox
    contents += backgroundButton
    contents += stopButton
  }

  updateProgress

  private val scanner:Actor = actor {
    while(true) {
      Thread.sleep(1237)
      updateProgress
    }
  }

  def updateProgress = {
    progressLabel.text = project.status.statusString

    project.status match {
      case Project.BuildingGp =>
        progressBar.indeterminate = true
      case Project.RunningSamples(numDone, total) =>
        progressBar.indeterminate = false
        progressBar.max = total
        progressBar.value = numDone
      case _ => // Do nothing.  We shouldn't get these!
    }
    this.pack
  }
  
}

