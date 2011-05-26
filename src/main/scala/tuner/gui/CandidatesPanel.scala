package tuner.gui

import tuner.Project
import tuner.gui.event.SliceChanged

class CandidatesPanel(project:Project) 
    extends TableSelectionPanel(project, project.candidates) {
  
  override def updateTable = {
    super.updateTable
    dataTable.selection.rows += 0
  }
}

