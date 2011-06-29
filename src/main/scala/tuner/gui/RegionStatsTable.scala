package tuner.gui

import scala.swing.Table

import tuner.project.Viewable

object RegionStatsTable {
  def columnNames(project:Viewable) = {
    List("","Samples") ++ (project.inputFields.map {fld =>
      fld + " Gradient"
    }).toSeq
  }
}

class RegionStatsTable(project:Viewable) 
    extends Table(Array.fill(project.responseFields.length)(
                    Array.fill(2+project.inputFields.length)("":Any)
                  ), RegionStatsTable.columnNames(project)) {
  
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

