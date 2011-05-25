package tuner.gui

import scala.swing.Table

import tuner.Project

class RegionStatsTable(project:Project) 
    extends Table(project.responseFields.length, 
                  2 + project.inputFields.length) {
  
  def updateStats = {
    project.responseFields.zipWithIndex.foreach {case (resp, row) =>
      update(row, 0, resp)
      update(row, 1, project.region.numSamples)
      project.inputFields.zipWithIndex.foreach {case (fld, i) =>
        update(row, i+2, project.region.gradient(resp, fld))
      }
    }
  }
}

