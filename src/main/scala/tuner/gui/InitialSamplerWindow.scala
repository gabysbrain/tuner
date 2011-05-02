package tuner.gui

import scala.swing.BorderPanel
import scala.swing.BoxPanel
import scala.swing.Button
import scala.swing.Label
import scala.swing.MainFrame
import scala.swing.Orientation
import scala.swing.Swing
import scala.swing.event.ButtonClicked

import tuner.Project
import tuner.Tuner

class InitialSamplerWindow(project:Project) extends MainFrame {
  
  title = "Select Samples"

  menuBar = MainMenu

  val prevButton = new Button("Prev")
  val clusterButton = new Button("Save for Cluster")
  val runButton = new Button("Run")

  val samplerPanel = new SamplerPanel

  val titlePanel = new BoxPanel(Orientation.Horizontal) {
    contents += Swing.HGlue
    contents += new Label("Step 2 of 2: Initial Sampling")
    contents += Swing.HGlue
  }

  val buttonPanel = new BoxPanel(Orientation.Horizontal) {
    contents += prevButton
    contents += Swing.HGlue
    contents += clusterButton
    contents += runButton
  }

  contents = new BorderPanel {
    layout(titlePanel) = BorderPanel.Position.North
    layout(samplerPanel) = BorderPanel.Position.Center
    layout(buttonPanel) = BorderPanel.Position.South
  }

  listenTo(runButton)

  reactions += {
    case ButtonClicked(`runButton`) =>
      close
      Tuner.openProject(project)
  }
}

