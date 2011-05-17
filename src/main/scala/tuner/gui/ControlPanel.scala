package tuner.gui

import scala.swing.Alignment
import scala.swing.TabbedPane

import tuner.Project

class ControlPanel(project:Project) extends TabbedPane {
  
  tabPlacement(Alignment.Left)

  val infoTab = new InfoPanel(project)
  val localTab = new LocalPanel
  val controlsTab = new PlotControlsPanel(project)
  val candidatesTab = new CandidatesPanel
  val historyTab = new HistoryPanel

  pages += new TabbedPane.Page("Info", infoTab)
  pages += new TabbedPane.Page("Local", localTab)
  pages += new TabbedPane.Page("Controls", controlsTab)
  pages += new TabbedPane.Page("Candidates", candidatesTab)
  pages += new TabbedPane.Page("History", historyTab)
}


