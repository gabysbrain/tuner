package tuner.gui

import scala.swing.BoxPanel
import scala.swing.ButtonGroup
import scala.swing.FlowPanel
import scala.swing.Label
import scala.swing.MainFrame
import scala.swing.Orientation
import scala.swing.RadioButton
import scala.swing.Swing
import scala.swing.TablePanel

import tuner.Project

class ProjectViewer(project:Project) extends MainFrame {
  
  title = "View Project"
  menuBar = MainMenu

  val mainResponseButton = new RadioButton("Value")
  val errResponseButton = new RadioButton("Error")
  val gainResponseButton = new RadioButton("Gain")

  new ButtonGroup(mainResponseButton, errResponseButton, gainResponseButton)

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
  
    val relativeValuePanel = new FlowPanel {
      border = Swing.TitledBorder(border, "Relative Value")
    }
  
    val histogramPanel = new FlowPanel {
      border = Swing.TitledBorder(border, "Response Histograms")
    }
  
    val mainPlotPanel = new FlowPanel {
      contents += new Label("Main Plot")
    }
  
    val controlPanel = new FlowPanel {
      contents += new Label("Controls")
    }

    val leftPanel = new BoxPanel(Orientation.Vertical) {
      contents += paretoPanel
      contents += responseControlPanel
      contents += relativeValuePanel
      contents += histogramPanel
    }

    val rightPanel = new BoxPanel(Orientation.Vertical) {
      contents += mainPlotPanel
      contents += controlPanel
    }

    layout(leftPanel) = (0,0)
    layout(rightPanel) = (1,0)
  }
}

