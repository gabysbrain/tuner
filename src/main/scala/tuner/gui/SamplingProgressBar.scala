package tuner.gui

import akka.actor.Actor
import akka.actor.Actor._
import scala.swing.BoxPanel
import scala.swing.Button
import scala.swing.CheckBox
import scala.swing.Dialog
import scala.swing.Label
import scala.swing.Orientation
import scala.swing.ProgressBar
import scala.swing.event.ButtonClicked
import scala.swing.event.UIElementResized

import tuner.ConsoleLine
import tuner.Progress
import tuner.ProgressComplete
import tuner.ProgressWarning
import tuner.project.InProgress
import tuner.project.Project
import tuner.project.Saved

class SamplingProgressBar(project:InProgress) extends Window(project) {

  //modal = true
  //width = 800
  //height = 75
  var errors = false

  val progressBar = new ProgressBar {
    min = 0
  }
  val alwaysBackgroundCheckbox = new CheckBox("Always Background") {
    selected = project.buildInBackground
    enabled = false
  }
  val backgroundButton = new Button("Background")
  backgroundButton.enabled = false
  val stopButton = new Button("Stop")
  val progressLabel = new Label {
    text = "   "
  }
  val console = new HideableConsole

  listenTo(project)
  listenTo(backgroundButton)
  listenTo(stopButton)
  listenTo(console)

  contents = new BoxPanel(Orientation.Vertical) {
    contents += new BoxPanel(Orientation.Horizontal) {
      contents += new BoxPanel(Orientation.Vertical) {
        contents += progressBar
        contents += progressLabel
      }

      contents += alwaysBackgroundCheckbox
      contents += backgroundButton
      contents += stopButton
    }
    contents += console
  }

  reactions += {
    case ButtonClicked(`backgroundButton`) =>
      project.buildInBackground = alwaysBackgroundCheckbox.selected
      project match {
        case s:Saved => s.save
        case _       =>
      }
      dispose
    case ButtonClicked(`stopButton`) => 
      //project.stop
      dispose
    case UIElementResized(_) =>
      this.pack
    case Progress(currentTime, totalTime, msg, ok) =>
      updateProgress(currentTime, totalTime, msg, ok)
    case ProgressWarning(msg) =>
      Dialog.showMessage(contents.head, msg, "Build warning", 
                         Dialog.Message.Warning)
    case ConsoleLine(line) => 
      console.text += line
      console.text += "\n"
    case ProgressComplete =>
      if(!errors) dispose
  }

  this.pack

  def updateProgress(cur:Int, max:Int, msg:String, ok:Boolean) = {
    if(ok) {
      progressLabel.foreground = java.awt.Color.black
      progressLabel.text = msg
      if(max > 0) {
        progressBar.indeterminate = false
        progressBar.max = max
        progressBar.value = cur
      } else {
        progressBar.indeterminate = true
      }
    } else {
      errors = true
      progressLabel.foreground = java.awt.Color.red
      progressLabel.text = "Error: " + msg
      console.text += msg + "\n"
      progressBar.indeterminate = true
    }

    this.pack
  }

}

