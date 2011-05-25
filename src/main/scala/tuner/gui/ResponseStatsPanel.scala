package tuner.gui

import scala.swing.BoxPanel
import scala.swing.CollapsiblePanel
import scala.swing.Orientation

import tuner.Project

class ResponseStatsPanel(project:Project) 
    extends BoxPanel(Orientation.Vertical) {
  
  val histogramPanels = project.gpModels.get.map {case (fld, model) =>
    val panel = new ResponseHistogramPanel(project, fld)
    (fld -> panel)
  }

  contents += new CollapsiblePanel {
    histogramPanels.foreach {case (fld,panel) =>
      //panels += new CollapsiblePanel.CPanel(fld, panel)
      val p = new org.japura.gui.CollapsiblePanel(fld)
      p.add(panel.peer)
      peer.add(p)
    }
  }
}

