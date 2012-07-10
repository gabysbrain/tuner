package tuner.gui

import scala.swing.BorderPanel
import scala.swing.BoxPanel
import scala.swing.Button
import scala.swing.Dialog
import scala.swing.Orientation
import scala.swing.Swing
import scala.swing.event.ButtonClicked
import scala.swing.event.DialogClosing
import scala.swing.event.ValueChanged

import tuner.project.Viewable

/**
 * The dialog for adding samples
 */
class SamplerDialog(project:Viewable, owner:scala.swing.Window) 
        extends Dialog(owner) {

  title = "Add Samples"
  modal = true

  //val okButton = new Button("
  val mainPanel = new SamplerPanel(project)
  val okButton = new Button("Run")
  val cancelButton = new Button("Cancel")

  listenTo(mainPanel)
  listenTo(okButton)
  listenTo(cancelButton)

  reactions += {
    case ValueChanged(`mainPanel`) =>
      project.newSamples(mainPanel.numSamples, 
                         project.region.toRange, 
                         mainPanel.method)
      mainPanel.splomPanel.redraw
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

