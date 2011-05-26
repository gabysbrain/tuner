package tuner.gui

import scala.swing.BoxPanel
import scala.swing.Orientation
import scala.swing.ScrollPane
import scala.swing.Table

import javax.swing.table.AbstractTableModel

import tuner.Project

class CandidatesPanel(project:Project) extends BoxPanel(Orientation.Vertical) {
  
  val columnNames = "Name" :: project.inputFields ++
                    project.responseFields.flatMap({rf =>
                      List(rf, rf + " Error", rf + " Gain")
                    })

  val tableModel = new AbstractTableModel {
    override def getColumnName(col:Int) = columnNames(col)
    def getColumnCount = columnNames.length
    def getRowCount = project.candidates.size
    def getValueAt(row:Int, col:Int) = {
      val itemName = project.candidates.names(row)
      val itemPoint = project.candidates.point(itemName)
      if(col == 0) { // The name of the item
        itemName
      } else if(col < project.inputFields.length + 1) {
        val field = project.inputFields(col - 1)
        itemPoint.toMap.get(field)
      } else {
        val adjCol = col - project.inputFields.length - 1
        val (fldNum, typeCode) = (adjCol / 3, adjCol % 3)
        val estimates = project.estimatePoint(itemPoint)
        val estField = project.responseFields(fldNum)
        typeCode match {
          case 0 => estimates(estField)._1.toString
          case 1 => estimates(estField)._2.toString
          case 2 => estimates(estField)._3.toString
        }
      }
    }
  }

  val candTable = new Table {
    model = tableModel
  }

  contents += new ScrollPane {
    contents = candTable
  }

  //override def maximumSize = preferredSize

  def updateTable = tableModel.fireTableStructureChanged
}

