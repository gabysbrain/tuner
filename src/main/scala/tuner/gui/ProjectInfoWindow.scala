package tuner.gui

import scala.swing.BorderPanel
import scala.swing.BoxPanel
import scala.swing.Button
import scala.swing.FileChooser
import scala.swing.Label
import scala.swing.MainFrame
import scala.swing.Orientation
import scala.swing.ScrollPane
import scala.swing.Swing
import scala.swing.TablePanel
import scala.swing.TextField
import scala.swing.event.ButtonClicked
import scala.swing.event.UIElementResized
import scala.swing.event.ValueChanged

import tuner.Config
import tuner.DimRanges
import tuner.Project
import tuner.Tuner
import tuner.gui.event.ControlTableRowChanged

class ProjectInfoWindow(project:Project) extends MainFrame {

  title = "New Project"

  menuBar = MainMenu

  resizable = false

  val projectNameField = new TextField
  val locationChooser = new PathPanel
  val scriptChooser = new PathPanel {
    fileSelectionMode = FileChooser.SelectionMode.FilesOnly
  }
  val nextButton = new Button("Next")
  val cancelButton = new Button("Cancel")

  val inputDimTable = new ControlTable(List("Name", "Lower", "Upper")) {
    def controlRow = List(
      new TextField,
      new TextField,
      new TextField
    )
  }

  val titlePanel = new BoxPanel(Orientation.Horizontal) {
    contents += Swing.HGlue
    contents += new Label("Step 1 of 2: Enter Initial Information")
    contents += Swing.HGlue
  }

  val contentPanel = new BoxPanel(Orientation.Vertical) {
    val projectInfoPanel = new TablePanel(List(90, TablePanel.Size.Fill),
                                          List(TablePanel.Size.Fill,TablePanel.Size.Fill)) {
      // Labels in the left column
      layout(new Label("Project Name")) = (0,0, TablePanel.HorizAlign.Right)
      layout(new Label("Save Location")) = (0,1, TablePanel.HorizAlign.Right)

      // Fields in the right column
      layout(projectNameField) = (1,0, TablePanel.HorizAlign.Full)
      layout(locationChooser) = (1,1, TablePanel.HorizAlign.Full)

      border = Swing.TitledBorder(border, "Project Info")
    }

    val scriptPanel = new TablePanel(List(90, TablePanel.Size.Fill),
                                     List(TablePanel.Size.Fill)) {
      layout(new Label("Script")) = (0,0, TablePanel.HorizAlign.Right)
      layout(scriptChooser) = (1,0, TablePanel.HorizAlign.Full)

      border = Swing.TitledBorder(border, "Black Box Interface")
    }

    val inputsPanel = new ScrollPane {
      contents = inputDimTable

      border = Swing.TitledBorder(border, "Inputs")
    }

    contents += projectInfoPanel
    contents += scriptPanel
    contents += inputsPanel
  }

  val buttonPanel = new BoxPanel(Orientation.Horizontal) {
    contents += Swing.HGlue
    contents += cancelButton
    contents += nextButton
  }

  contents = new BorderPanel {
    layout(titlePanel) = BorderPanel.Position.North
    layout(contentPanel) = BorderPanel.Position.Center
    layout(buttonPanel) = BorderPanel.Position.South
  }

  // Set up the interactions
  listenTo(nextButton)
  listenTo(cancelButton)
  listenTo(projectNameField)
  listenTo(scriptChooser)
  listenTo(inputDimTable)

  reactions += {
    case ButtonClicked(`nextButton`) =>
      updateInputDims
      val samplerWindow = 
        new InitialSamplerWindow(project, locationChooser.path)
      close
      samplerWindow.visible = true
    case ButtonClicked(`cancelButton`) => 
      close
      Tuner.top
    case ControlTableRowChanged(`inputDimTable`, _) =>
      updateInputDims
    case ValueChanged(`projectNameField`) =>
      project.name = projectNameField.text
    case ValueChanged(`scriptChooser`) =>
      project.scriptPath = Some(scriptChooser.path)
    case UIElementResized(`inputDimTable`) =>
      this.pack
  }

  def updateInputDims = {
    val controlValues = inputDimTable.controls.map {row =>
      val nameField = row(0).asInstanceOf[TextField]
      val minField = row(1).asInstanceOf[TextField]
      val maxField = row(2).asInstanceOf[TextField]
      if(nameField.text.length > 0 &&
         minField.text.length > 0 &&
         maxField.text.length > 0) {
        // Any conversion problems we ignore
        try {
          Some((nameField.text, (minField.text.toFloat, maxField.text.toFloat)))
        } catch {
          case _ => None
        }
      } else {
        None
      }
    }
    val validValues = controlValues.flatten
    if(validValues.length > 0) {
      project.inputs = new DimRanges(validValues.toMap)
    }
  }
}

