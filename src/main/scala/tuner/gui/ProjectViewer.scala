package tuner.gui

import scala.swing.BoxPanel
import scala.swing.ButtonGroup
import scala.swing.Dialog
import scala.swing.FlowPanel
import scala.swing.Label
import scala.swing.MainFrame
import scala.swing.Orientation
import scala.swing.RadioButton
import scala.swing.Swing
import scala.swing.TablePanel
import scala.swing.event.DialogClosing

import tuner.Project

class ProjectViewer(project:Project) extends MainFrame {
  
  title = project.name
  menuBar = MainMenu

  val mainResponseButton = new RadioButton("Value")
  val errResponseButton = new RadioButton("Error")
  val gainResponseButton = new RadioButton("Gain")

  new ButtonGroup(mainResponseButton, errResponseButton, gainResponseButton)

  val plot = new MainPlotPanel(project, Some("Precision"), Some("Precision"))

  contents = new TablePanel(List(305,TablePanel.Size.Fill), 
                            List(TablePanel.Size.Fill)) {
    val paretoPanel = new FlowPanel {
      border = Swing.TitledBorder(border, "Pareto")
    }
  
    val responseControlPanel = new BoxPanel(Orientation.Horizontal) {
      contents += mainResponseButton
      contents += errResponseButton
      contents += gainResponseButton
  
      border = Swing.TitledBorder(border, "View")
    }
  
    val histogramPanel = new FlowPanel {
      border = Swing.TitledBorder(border, "Response Histograms")
    }
  
    val mainPlotPanel = plot
  
    val controlPanel = new ControlPanel(project)

    val leftPanel = new BoxPanel(Orientation.Vertical) {
      contents += paretoPanel
      contents += responseControlPanel
      contents += histogramPanel
    }

    val rightPanel = new BoxPanel(Orientation.Vertical) {
      contents += mainPlotPanel
      contents += controlPanel
    }

    layout(leftPanel) = (0,0)
    layout(rightPanel) = (1,0)
  }

  override def visible_=(b:Boolean) = {
    super.visible_=(b)
    // See if there are new responses we need to deal with
    if(b && project.newFields.length > 0) {
      println("new fields detected")
      val rs = new ResponseSelector(project, this)
      listenTo(rs)
      reactions += {
        case DialogClosing(`rs`, result) => result match {
          case Dialog.Result.Ok => 
            rs.selections.foreach {case (fld, sel) => sel match {
              case ResponseSelector.Ignore => 
                project.ignoreFields = fld :: project.ignoreFields
              case ResponseSelector.Minimize => 
                project.responses = (fld, true) :: project.responses
              case ResponseSelector.Maximize => 
                project.responses = (fld, false) :: project.responses
            }}
            project.save(project.savePath)
          case Dialog.Result.Cancel =>
            this.close
        }
      }
      rs.open
    }
  }
}

