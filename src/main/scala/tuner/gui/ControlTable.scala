package tuner.gui

import scala.swing.Action
import scala.swing.Button
import scala.swing.Component
import scala.swing.Label
import scala.swing.TablePanel
import scala.swing.event.ButtonClicked

abstract class ControlTable(header0:List[String]) 
    extends TablePanel(header0.length+1, 1) {

  def controlRow : List[Component]

  val plusButton = new Button("+")

  // add the header row
  header0.zipWithIndex foreach {case (hdr, i) =>
    layout(new Label(hdr)) = (i, 0)
  }

  // now set up the first row
  addControlRow

  protected def addControlRow = {
    val controls = controlRow

    // We might need to change the plus to a minus button
    if(rows > 1) {
      val curRow = rows - 1
      val minusButton = new Button(Action("-") {dropRow(curRow)})
      layout(minusButton) = (controls.length, curRow)
    }

    addRow(TablePanel.Size.Fill)
    controls.zipWithIndex foreach {case (control, i) =>
      layout(control) = (i, rows-1)
    }
    layout(plusButton) = (controls.length, rows-1)
  }

  listenTo(plusButton)

  reactions += {
    case ButtonClicked(`plusButton`) => addControlRow
  }
}

