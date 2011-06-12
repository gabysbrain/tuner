package tuner.gui

import scala.swing.BorderPanel
import scala.swing.BoxPanel
import scala.swing.Button
import scala.swing.Dialog
import scala.swing.Orientation
import scala.swing.Swing
import scala.swing.Window
import scala.swing.event.ButtonClicked
import scala.swing.event.DialogClosing

import tuner.Project

class SamplerDialog(project:Project, owner:Window) extends Dialog(owner) {

  title = "Add Samples"
  modal = true

  //val okButton = new Button("
  val mainPanel = new SamplerPanel(project)
  val okButton = new Button("Run")
  val cancelButton = new Button("Cancel")

  listenTo(okButton)
  listenTo(cancelButton)

  reactions += {
    case ButtonClicked(`okButton`) =>
      publish(new DialogClosing(this, Dialog.Result.Ok))
      close
    case ButtonClicked(`cancelButton`) =>
      publish(new DialogClosing(this, Dialog.Result.Cancel))
      close
  }

  contents = new BorderPanel {
    val buttonPanel = new BoxPanel(Orientation.Horizontal) {
      contents += Swing.HGlue
      contents += okButton
      contents += cancelButton
    }
    layout(mainPanel) = BorderPanel.Position.Center
    layout(buttonPanel) = BorderPanel.Position.South
  }

  def numSamples = mainPanel.numSamples
  def method = mainPanel.method
}

