package tuner.gui

import scala.swing.Action
import scala.swing.Button
import scala.swing.Component
import scala.swing.Label
import scala.swing.TablePanel
import scala.swing.event.ButtonClicked
import scala.swing.event.ComponentEvent

import tuner.gui.event.ControlTableRowAdded
import tuner.gui.event.ControlTableRowChanged

abstract class ControlTable(header0:List[String], initialRows:Int=1) 
    extends TablePanel(header0.length+1, 1) {

  def controlRow : List[Component]

  val plusButton = new Button("+")

  // add the header row
  header0.zipWithIndex foreach {case (hdr, i) =>
    layout(new Label(hdr)) = (i, 0)
  }

  // now set up the first row
  1 to initialRows foreach {i => addControlRow}

  def addControlRow = {
    val controls = controlRow

    // We might need to change the plus to a minus button
    if(rows > 1) {
      val curRow = rows - 1
      val minusButton = new Button(Action("-") {
        rowControls(curRow).foreach {deafTo(_)}
        dropRow(curRow)
      })
      layout(minusButton) = (controls.length, curRow)
    }

    addRow(TablePanel.Size.Fill)
    controls.zipWithIndex foreach {case (control, i) =>
      layout(control) = (i, rows-1, TablePanel.HorizAlign.Full)
      listenTo(control)
    }
    layout(plusButton) = (controls.length, rows-1)
  }

  listenTo(plusButton)

  reactions += {
    case ButtonClicked(`plusButton`) => 
      addControlRow
      publish(new ControlTableRowAdded(this))
    case ce:ComponentEvent => findControlRow(ce.source) foreach {row =>
      publish(new ControlTableRowChanged(this, rowControls(row).toList))
    }
  }

  def findControlRow(c:Component) : Option[Int] = {
    contents.foldLeft(None:Option[Int]) {(loc,comp) =>
      if(comp == c)
        Some(constraintsFor(comp).ulRow)
      else
        None
    }
  }

  def rowControls(row:Int) = {
    val rowControls = contents.filter {c => constraintsFor(c).ulRow == row}
    rowControls.sortBy(constraintsFor(_).ulCol) init
  }

  def controls = {
    (1 until rows).map {r => rowControls(r)}
  }
}

