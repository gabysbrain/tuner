package tuner.gui

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

  val progressBar = new ProgressBar
  val alwaysBackgroundCheckbox = new CheckBox("Always Background")
  val backgroundButton = new Button("Background")
  val stopButton = new Button("Stop")
  val progressLabel = new Label

  contents = new BoxPanel(Orientation.Horizontal) {
    contents += new BoxPanel(Orientation.Vertical) {
      contents += progressBar
      contents += progressLabel
    }

    contents += alwaysBackgroundCheckbox
    contents += backgroundButton
    contents += stopButton
  }

}

