package tuner.gui

import scala.swing.BoxPanel
import scala.swing.Orientation
import scala.swing.ScrollPane
import scala.swing.Table
import scala.swing.event.TableRowsSelected

import javax.swing.table.AbstractTableModel

import tuner.Project
import tuner.gui.event.SliceChanged

class HistoryPanel(project:Project) extends BoxPanel(Orientation.Vertical) {
  
  val columnNames = "Name" :: project.inputFields ++
                    project.responseFields.flatMap({rf =>
                      List(rf, rf + " Error", rf + " Gain")
                    })

  val tableModel = new AbstractTableModel {
    override def getColumnName(col:Int) = columnNames(col)
    def getColumnCount = columnNames.length
    def getRowCount = project.history.size
    def getValueAt(row:Int, col:Int) = {
      val itemName = project.history.names(row)
      val itemPoint = project.history.point(itemName)
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

  val histTable = new Table {
    model = tableModel
  }

  contents += new ScrollPane {
    contents = histTable
  }

  listenTo(histTable.selection)

  // Need to keep track of the last row clicked 
  // because we get an event from a row being 
  // deselected and then selected
  var lastRow = -1

  reactions += {
    case TableRowsSelected(`histTable`, _, _) =>
      val row = histTable.selection.rows.leadIndex
      if(row != lastRow) {
        lastRow = row
        val itemName = project.history.names(row)
        val point = project.history.point(itemName)
        publish(new SliceChanged(this, point))
      }
  }

  

  //override def maximumSize = preferredSize

  def updateTable = tableModel.fireTableStructureChanged
}

