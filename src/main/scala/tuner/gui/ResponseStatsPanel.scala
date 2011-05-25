package tuner.gui

import scala.swing.BoxPanel
import scala.swing.Orientation

import tuner.Project

class ResponseStatsPanel(project:Project) 
    extends BoxPanel(Orientation.Vertical) {
  
  val histogramPanels = project.gpModels.get.map {case (fld, model) =>
    val panel = new ResponseHistogramPanel(project, fld)
    contents += panel
    (fld -> panel)
  }
}

