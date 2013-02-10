package tuner.gui

import scala.swing.BorderPanel
import scala.swing.BoxPanel
import scala.swing.Button
import scala.swing.Label
import scala.swing.Frame
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
import tuner.Tuner
import tuner.gui.event.ControlTableRowAdded
import tuner.gui.event.ControlTableRowChanged
import tuner.project.NewProject

/**
 * The first window for setting up a new project.  Asks for things like
 * the project name, sampling script, and input parameter information.
 */
class NewProjectWindow extends Frame {

  title = "New Project"

  menuBar = new MainMenu

  resizable = true

  val projectNameField = new TextField
  val locationChooser = new PathPanel {
    fileSelector = FileChooser.loadDirectory _
  }
  val scriptChooser = new PathPanel
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
    contents += Swing.HGlue
    contents += scriptPanel
    contents += Swing.HGlue
    contents += inputsPanel
    contents += Swing.HGlue
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
      val samplerWindow = 
        new InitialSamplerWindow(createProject, locationChooser.path)
      close
      samplerWindow.visible = true
    case ButtonClicked(`cancelButton`) => 
      close
      Tuner.top
    case ControlTableRowAdded(`inputDimTable`) =>
      this.pack
  }

  def createProject : NewProject = {
    val name = projectNameField.text
    val scriptPath = scriptChooser.path
    val inputs = inputDims
    new NewProject(name, locationChooser.path, scriptPath, inputs)
  }

  def inputDims : List[(String,Float,Float)] = {
    val controlValues = inputDimTable.controls.map {row =>
      val nameField = row(0).asInstanceOf[TextField]
      val minField = row(1).asInstanceOf[TextField]
      val maxField = row(2).asInstanceOf[TextField]
      if(nameField.text.length > 0 &&
         minField.text.length > 0 &&
         maxField.text.length > 0) {
        // Any conversion problems we ignore
        try {
          Some((nameField.text.trim, 
                minField.text.toFloat, 
                maxField.text.toFloat))
        } catch {
          case _ => None
        }
      } else {
        None
      }
    }
    controlValues.flatten.toList
  }

}

