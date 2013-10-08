package tuner.gui

import scala.swing.ComboBox
import scala.swing.Label
import scala.swing.TablePanel
import scala.swing.event.SelectionChanged
import scala.swing.event.ValueChanged

import tuner.gui.event.NewDesignSelected
import tuner.gui.event.NewResponseSelected

import tuner.Sampler

/**
 * A panel allowing a user to select a file of already run samples to import
 */
class SampleImportPanel(newSamples:((Int, Sampler.Method) => Unit))
  extends TablePanel(2, 2) {

  val fileChooser = new PathPanel
  var valueSelector = new ComboBox(List("None")) {
    enabled = false
  }

  // Layouts
  layout(new Label("Sample File")) = (0,0)
  layout(new Label("Response Value")) = (0,1)
  layout(fileChooser) = (1,0)
  layout(valueSelector) = (1,1)

  // Event setup
  listenTo(fileChooser)
  listenTo(valueSelector.selection)

  reactions += {
    case ValueChanged(`fileChooser`) => if(fileChooser.validPath) {
      publish(new NewDesignSelected(this))
    }
    case SelectionChanged(_) => 
      publish(new NewResponseSelected(this, valueSelector.selection.item))
  }

  def responses : Seq[String] = List()
  def responses_=(r:Seq[String]) = if(r.isEmpty) {
    valueSelector.enabled = false
  } else {
    // No direct way to change the contents of a combobox
    valueSelector = new ComboBox(r)
    valueSelector.enabled = true
    valueSelector.selection.index = 0
  }
  def designPath : String = fileChooser.path
  def designFile : java.io.File = fileChooser.file
  def selectedResponse : Option[String] = if(valueSelector.enabled) {
    Some(valueSelector.selection.item)
  } else {
    None
  }
}

