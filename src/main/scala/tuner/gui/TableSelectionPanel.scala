package tuner.gui

import scala.swing.BoxPanel
import scala.swing.Orientation
import scala.swing.ScrollPane
import scala.swing.Table
import scala.swing.event.TableRowsSelected

import javax.swing.table.AbstractTableModel

import tuner.NamedPointManager
import tuner.gui.event.SliceChanged
import tuner.project.Viewable

class TableSelectionPanel(project:Viewable, points:NamedPointManager) 
    extends BoxPanel(Orientation.Vertical) {
  
  val columnNames = "Name" :: project.inputFields ++
                    project.responseFields.flatMap({rf =>
                      List(rf, rf + " Error", rf + " Gain")
                    })

  val tableModel = new AbstractTableModel {
    override def getColumnName(col:Int) = columnNames(col)
    def getColumnCount = columnNames.length
    def getRowCount = points.size
    def getValueAt(row:Int, col:Int) = {
      val itemName = points.names(row)
      val itemPoint = points.point(itemName)
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

  val dataTable = new Table {
    model = tableModel
    autoResizeMode = Table.AutoResizeMode.Off
  }

  contents += new ScrollPane {
    contents = dataTable
  }

  listenTo(dataTable.selection)

  // Need to keep track of the last row clicked 
  // because we get an event from a row being 
  // deselected and then selected
  var lastRow = -1

  reactions += {
    case TableRowsSelected(`dataTable`, _, _) =>
      val row = dataTable.selection.rows.leadIndex
      // -1 means no rows selected
      if(row != -1 && row != lastRow) {
        lastRow = row
        val itemName = points.names(row)
        val point = points.point(itemName)
        publish(new SliceChanged(this, point))
      } else if(row == -1) {
        lastRow = row
      }
  }

  

  def updateTable = tableModel.fireTableStructureChanged
}

