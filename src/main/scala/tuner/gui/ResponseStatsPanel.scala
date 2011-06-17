package tuner.gui

import scala.swing.BoxPanel
import scala.swing.CollapsiblePanel
import scala.swing.Orientation
import scala.swing.ScrollPane

import tuner.Project
import tuner.gui.event.ReadyToDraw

class ResponseStatsPanel(project:Project) 
    extends BoxPanel(Orientation.Vertical) {
    //extends BoxPanel(Orientation.Vertical) {
  
  var curMin = Float.MaxValue
  var curMax = Float.MinValue

  val histogramPanels:Map[String,ResponseHistogramPanel] = project.gpModels match {
    case Some(gpm) => project.responseFields.map {fld =>
      val model = gpm(fld)
      val panel = new ResponseHistogramPanel(project, fld)
      listenTo(panel)
      (fld -> panel)
    } toMap
    case None      => Map()
  }

  //contents = new BoxPanel(Orientation.Vertical) {
    /*
    contents += new CollapsiblePanel(CollapsiblePanel.Scroll) {
      histogramPanels.foreach {case (fld,panel) =>
        //panels += new CollapsiblePanel.CPanel(fld, panel)
        val p = new org.japura.gui.CollapsiblePanel(fld)
        p.collapse
        p.setAnimationEnabled(false)
        p.addCollapsiblePanelListener(new org.japura.gui.event.CollapsiblePanelAdapter {
          override def panelCollapsed(event:org.japura.gui.event.CollapsiblePanelEvent) = {
            panel.visible = false
          }
          override def panelExpanded(event:org.japura.gui.event.CollapsiblePanelEvent) = {
            panel.visible = true
          }
        })
        p.add(panel.peer)
        peer.add(p)
      }
    }
    */
  //}

  reactions += {
    case ReadyToDraw(rhp:ResponseHistogramPanel) =>
      updateHistograms(rhp)
  }

  protected def updateHistograms(updatedPanel:ResponseHistogramPanel) = {
    curMin = math.min(curMin, updatedPanel.yAxisTicks.min)
    curMax = math.max(curMax, updatedPanel.yAxisTicks.max)
    val newTicks = List(curMin, (curMin+curMax)/2, curMax)
    histogramPanels.values.foreach {panel =>
      panel.yAxisTicks = newTicks
    }
  }
}

