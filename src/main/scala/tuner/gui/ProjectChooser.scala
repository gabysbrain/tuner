package tuner.gui

import scala.swing.BorderPanel
import scala.swing.BoxPanel
import scala.swing.Button
import scala.swing.FlowPanel
import scala.swing.MainFrame
import scala.swing.Orientation
import scala.swing.Swing
import scala.swing.Table
import scala.swing.event.ButtonClicked

import tuner.Tuner

/**
 * Project window that allows the user to select which project they want to
 * examine.
 */
object ProjectChooser extends MainFrame {

  title = "Select Project"

  menuBar = MainMenu

  // All the buttons
  val newProjectButton = new Button("New Project")
  val loadFromClusterButton = new Button("Load from Cluster") {
    enabled = false
  }
  val openOtherButton = new Button("Open Other") {
    enabled = false
  }
  val openButton = new Button("Open") {
    enabled = false
  }

  // The project list table
  val projectTable = new Table

  // Set up the rest of the UI
  val tablePanel = new FlowPanel {
    contents += projectTable

    border = Swing.TitledBorder(border, "Recent Projects")
  }

  val buttonPanel = new BoxPanel(Orientation.Horizontal) {
    contents += newProjectButton
    contents += Swing.HGlue
    contents += loadFromClusterButton
    contents += openOtherButton
    contents += openButton
  }

  contents = new BorderPanel {
    layout(tablePanel) = BorderPanel.Position.Center
    layout(buttonPanel) = BorderPanel.Position.South
  }

  defaultButton = openButton

  // Set up the events processing
  listenTo(newProjectButton)
  listenTo(loadFromClusterButton)
  listenTo(openOtherButton)
  listenTo(openButton)

  reactions += {
    case ButtonClicked(`newProjectButton`) => Tuner.startNewProject
  }

}

