package tuner.gui

import scala.swing.Dialog
import scala.swing.Window

import tuner.Project

class SamplerDialog(project:Project, owner:Window) extends Dialog(owner) {

  title = "Add Samples"
  modal = true

  //val okButton = new Button("
  val mainPanel = new SamplerPanel(project)
}

