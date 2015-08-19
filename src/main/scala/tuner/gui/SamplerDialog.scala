package tuner.gui

import scala.swing.BorderPanel
import scala.swing.BoxPanel
import scala.swing.Button
import scala.swing.Dialog
import scala.swing.Orientation
import scala.swing.Swing
import scala.swing.event.ButtonClicked
import scala.swing.event.ValueChanged
import tuner.gui.event.DialogClosing

import tuner.Sampler
import tuner.project.Viewable
import tuner.util.Path

/**
 * The dialog for adding samples
 */
class SamplerDialog(project:Viewable, owner:scala.swing.Window) 
        extends Dialog(owner) {

  title = "Add Samples"
  modal = true

  def newSamples(num:Int, method:Sampler.Method) = {
    project.newSamples(num, project.region.toRange, method)
  }
  //val okButton = new Button("
  val mainPanel = new SamplerPanel(project, newSamples)
  val saveButton = new Button("Save for cluster")
  val okButton = new Button("Run")
  val cancelButton = new Button("Cancel")

  listenTo(mainPanel)
  listenTo(saveButton)
  listenTo(okButton)
  listenTo(cancelButton)

  reactions += {
    case ButtonClicked(`okButton`) =>
      publish(new DialogClosing(this, Dialog.Result.Ok))
      dispose
    case ButtonClicked(`saveButton`) =>
      mainPanel.saveSamples
      //project.save()
      project.clearSamples
      publish(new DialogClosing(this, Dialog.Result.Ok))
      //owner.close
      dispose
    case ButtonClicked(`cancelButton`) =>
      publish(new DialogClosing(this, Dialog.Result.Cancel))
      dispose
  }

  contents = new BorderPanel {
    val buttonPanel = new BoxPanel(Orientation.Horizontal) {
      contents += Swing.HGlue
      contents += saveButton
      contents += okButton
      contents += cancelButton
    }
    layout(mainPanel) = BorderPanel.Position.Center
    layout(buttonPanel) = BorderPanel.Position.South
  }
}

