package tuner.gui

import tuner.gui.event.SliceChanged
import tuner.project.Viewable

class CandidatesPanel(project:Viewable) 
    extends TableSelectionPanel(project, project.candidates) {
  
  override def updateTable = {
    super.updateTable
    dataTable.selection.rows += 0
  }
}

