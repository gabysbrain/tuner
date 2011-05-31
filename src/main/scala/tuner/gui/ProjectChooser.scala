package tuner.gui

import scala.swing.BorderPanel
import scala.swing.BoxPanel
import scala.swing.Button
import scala.swing.FileChooser
import scala.swing.MainFrame
import scala.swing.Orientation
import scala.swing.ScrollPane
import scala.swing.Swing
import scala.swing.Table
import scala.swing.event.ButtonClicked
import scala.swing.event.TableRowsSelected

import javax.swing.table.AbstractTableModel

import tuner.Project
import tuner.Tuner

/**
 * Project window that allows the user to select which project they want to
 * examine.
 */
object ProjectChooser extends MainFrame {

  title = "Select Project"

  menuBar = MainMenu
  centerOnScreen

  // All the buttons
  val newProjectButton = new Button("New Project")
  val openOtherButton = new Button("Open Other") {
    enabled = true
  }
  val openButton = new Button("Open") {
    enabled = false
  }

  // The project list table
  val projectTable = new Table {
    val columnNames = List("Name", "Last Modified", "Status")
    val rows = Project.recent

    model = new AbstractTableModel {
      override def getColumnName(col:Int) = columnNames(col)
      def getColumnCount = columnNames.length
      def getRowCount = rows.length
      def getValueAt(row:Int, col:Int) = col match {
        case 0 => rows(row).name
        case 1 => rows(row).modificationDate
        case 2 => rows(row).status
      }
    }
  }

  // Set up the rest of the UI
  val tablePanel = new ScrollPane {
    contents = projectTable

    border = Swing.TitledBorder(border, "Recent Projects")
  }

  val buttonPanel = new BoxPanel(Orientation.Horizontal) {
    contents += newProjectButton
    contents += Swing.HGlue
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
  listenTo(openOtherButton)
  listenTo(openButton)
  listenTo(projectTable.selection)

  reactions += {
    case ButtonClicked(`newProjectButton`) => Tuner.startNewProject
    case ButtonClicked(`openOtherButton`) => openOtherProject
    case ButtonClicked(`openButton`) => openSelectedProject
    case TableRowsSelected(`projectTable`, _, _) =>
      val row = projectTable.selection.rows.leadIndex
      openButton.enabled = row != -1
  }

  def openOtherProject = {
    val fc = new FileChooser {
      title = "Select Project"
      fileSelectionMode = FileChooser.SelectionMode.DirectoriesOnly
    }
    fc.showOpenDialog(projectTable) match {
      case FileChooser.Result.Approve => Tuner.openProject(fc.selectedFile)
      case _ =>
    }
  }

  def openSelectedProject = {
    val row = projectTable.selection.rows.leadIndex
    val proj = Project.recent(row)
    Tuner.openProject(proj)
  }

}

