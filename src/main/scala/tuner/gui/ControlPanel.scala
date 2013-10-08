package tuner.gui

import scala.swing.Alignment
import scala.swing.TabbedPane
import scala.swing.event.ValueChanged

import java.awt.Dimension

import tuner.Config
import tuner.project.Viewable

/**
 * Panel containing Tuner application controls.
 * 
 * Displayed underneath main slice view 
 */
class ControlPanel(project:Viewable) extends TabbedPane {
  
  val dims = new Dimension(Int.MaxValue, Config.controlPanelHeight)

  tabPlacement(Alignment.Top)
  maximumSize = new Dimension(Int.MaxValue, Config.controlPanelHeight)

  val infoTab = new InfoPanel(project) {
    preferredSize = dims
    maximumSize = dims
  }
  val localTab = new LocalPanel(project) {
    preferredSize = dims
    maximumSize = dims
  }
  val controlsTab = new PlotControlsPanel(project) {
    preferredSize = dims
    maximumSize = dims
  }
  /*
  val candidatesTab = new CandidatesPanel(project) {
    preferredSize = dims
    maximumSize = dims
  }
  */
  val historyTab = new HistoryPanel(project) {
    preferredSize = dims
    maximumSize = dims
  }

  pages += new TabbedPane.Page("Info", infoTab)
  pages += new TabbedPane.Page("Local", localTab)
  pages += new TabbedPane.Page("Controls", controlsTab)
  //pages += new TabbedPane.Page("Candidates", candidatesTab)
  pages += new TabbedPane.Page("History", historyTab)

  // We need to update the info panel when the slice changes
  controlsTab.sliceSliders.values.foreach {slider =>
    listenTo(slider)
  }

  reactions += {
    case ValueChanged(_) => infoTab.updateView
  }

}


