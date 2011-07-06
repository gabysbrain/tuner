package tuner.gui

import scala.swing.BoxPanel
import scala.swing.CollapsiblePanel
import scala.swing.Orientation
import scala.swing.ScrollPane

import scala.collection.immutable.SortedMap

//import tuner.gui.event.ReadyToDraw
import tuner.project.Viewable

class ResponseStatsPanel(project:Viewable) 
    extends ScrollPane {
    //extends BoxPanel(Orientation.Vertical) {
  
  val histogramPanels:SortedMap[String,ResponseHistogramPanel] = {
    val gpm = project.gpModels
    SortedMap[String,ResponseHistogramPanel]() ++
      project.responseFields.map {fld =>
        val model = gpm(fld)
        val panel = new ResponseHistogramPanel(project, fld)
        listenTo(panel)
        (fld -> panel)
      }
  }

  contents = new BoxPanel(Orientation.Vertical) {
    contents += new CollapsiblePanel(CollapsiblePanel.Scroll) {
      histogramPanels.foreach {case (fld,panel) =>
        //panels += new CollapsiblePanel.CPanel(fld, panel)
        val p = new org.japura.gui.CollapsiblePanel(fld)
        p.addCollapsiblePanelListener(new org.japura.gui.event.CollapsiblePanelAdapter {
          override def panelCollapsed(event:org.japura.gui.event.CollapsiblePanelEvent) = {
            panel.visible = false
          }
          override def panelExpanded(event:org.japura.gui.event.CollapsiblePanelEvent) = {
            panel.visible = true
          }
        })
        p.collapseImmediately
        p.setAnimationEnabled(false)
        p.add(panel.peer)
        peer.add(p)
      }
    }
  }

  /*
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
  */
}

