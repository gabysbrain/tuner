package tuner.gui

import scala.swing.BoxPanel
import scala.swing.Button
import scala.swing.Dialog
import scala.swing.Orientation
import scala.swing.RadioButton
import scala.swing.TextField

class ResponseSelector extends Dialog {
  title = "Select Responses"

  val okButton = new Button("Ok")
  val cancelButton = new Button("Cancel")

  val responseTable = new ControlTable(List("Name", "Maximize")) {
    def controlRow = List(
      new TextField,
      new RadioButton("Min"),
      new RadioButton("Max")
    )
  }

  contents = new BoxPanel(Orientation.Vertical) {
    contents += responseTable
    contents += okButton
    contents += cancelButton
  }
}

