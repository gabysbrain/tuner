package tuner.gui

import scala.swing.BorderPanel
import scala.swing.BoxPanel
import scala.swing.Button
import scala.swing.Label
import scala.swing.MainFrame
import scala.swing.Orientation
import scala.swing.Swing
import scala.swing.TextField

class ProjectInfoWindow extends MainFrame {

  title = "New Project"

  menuBar = MainMenu

  val projectNameField = new TextField
  val locationChooser = new FileChooser
  val scriptChooser = new FileChooser
  val nextButton = new Button("Next")
  val cancelButton = new Button("Cancel")

  val titlePanel = new BoxPanel(Orientation.Horizontal) {
    contents += Swing.HGlue
    contents += new Label("Step 1 of 2: Enter Initial Information")
    contents += Swing.HGlue
  }

  val contentPanel = new BoxPanel(Orientation.Vertical) {
    val projectInfoPanel = new BoxPanel(Orientation.Vertical) {
      contents += projectNameField
      contents += locationChooser
    }

    val scriptPanel = new BoxPanel(Orientation.Vertical) {
      contents += scriptChooser
    }

    contents += projectInfoPanel
    contents += scriptPanel
  }

  val buttonPanel = new BoxPanel(Orientation.Horizontal) {
    contents += Swing.HGlue
    contents += cancelButton
    contents += nextButton
  }

  contents = new BorderPanel {
    layout(titlePanel) = BorderPanel.Position.North
    layout(contentPanel) = BorderPanel.Position.Center
  }
}

