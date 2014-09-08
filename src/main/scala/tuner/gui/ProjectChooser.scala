package tuner.gui

import scala.swing.BorderPanel
import scala.swing.BoxPanel
import scala.swing.Button
import scala.swing.Frame
import scala.swing.Orientation
import scala.swing.ScrollPane
import scala.swing.Swing
import scala.swing.Table
import scala.swing.event.ButtonClicked
import scala.swing.event.TableRowsSelected
import scala.swing.event.WindowActivated

import javax.swing.table.AbstractTableModel

import tuner.Tuner
import tuner.project.Project

/**
 * Project window that allows the user to select which project they want to
 * examine.
 */
object ProjectChooser extends Frame {

  var projects = Project.recent

  title = "Select Project"

  val myMenu = new MainMenu
  menuBar = myMenu
  centerOnScreen

  override def closeOperation() {
    Tuner.maybeQuit
  }

  // All the buttons
  val newProjectButton = new Button("New Project")
  val openOtherButton = new Button("Open Other") {
    enabled = true
  }
  val openButton = new Button("Open") {
    enabled = false
  }

  // The project list table
  val projectTableModel = new AbstractTableModel {
    val columnNames = List("Name", "Last Modified", "Status")

    override def getColumnName(col:Int) = columnNames(col)
    def getColumnCount = columnNames.length
    def getRowCount = projects.size
    def getValueAt(row:Int, col:Int) = col match {
      case 0 => projects(row).name
      case 1 => projects(row).modificationDate
      case 2 => projects(row).statusString
    }
  }
  val projectTable = new Table {
    model = projectTableModel
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

  val importSamplesItem = myMenu.importSamples
  reactions += {
    case WindowActivated(x) => updateProjects
    case ButtonClicked(`newProjectButton`) => Tuner.startNewProject
    case ButtonClicked(`openOtherButton`) => openOtherProject
    case ButtonClicked(`openButton`) => openSelectedProject
    case ButtonClicked(`importSamplesItem`) =>
      projects.foreach {proj => proj match {
        case sp:tuner.project.Saved => sp.save()
        case _ =>
      }}
      updateProjects
    case TableRowsSelected(`projectTable`, _, _) =>
      val row = projectTable.selection.rows.leadIndex
      openButton.enabled = row != -1
      importSamplesItem.enabled = false
      if(row != -1) {
        val proj = projects(row)
        proj match {
          case p:tuner.project.Sampler =>
            importSamplesItem.action = MainMenu.ImportSamplesAction(p)
            importSamplesItem.enabled = true
          case _ =>
        }
      }
  }

  def openOtherProject = {
    Tuner.openProject
    updateProjects
  }

  def openSelectedProject = {
    val row = projectTable.selection.rows.leadIndex
    val proj = projects(row)
    Tuner.openProject(proj)
  }

  protected def updateProjects = {
    projects = Project.recent
    projectTableModel.fireTableDataChanged
  }

}
