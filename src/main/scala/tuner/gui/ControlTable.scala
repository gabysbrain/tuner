package tuner.gui

import scala.swing.Button
import scala.swing.Component
import scala.swing.GridBagPanel
import scala.swing.Label
import scala.swing.event.ButtonClicked

abstract class ControlTable(header0:List[String]) extends GridBagPanel {

  def controlRow : List[Component]

  // need to track the number of rows
  var numRows = 0

  val plusButton = new Button("+")

  // add the header row
  header0.zipWithIndex foreach {case (hdr, i) =>
    layout(new Label(hdr)) = (i,0)
  }

  // now set up the first row
  addRow

  protected def addRow = {
    val controls = controlRow

    // We might need to change the plus to a minus button
    if(numRows > 0) {
      layout(new Button("-")) = (controls.length, numRows)
    }

    val row = numRows + 1
    controls.zipWithIndex foreach {case (c, i) =>
      layout(c) = (i, row)
    }
    layout(plusButton) = (controls.length, row)

    numRows += 1
  }

  listenTo(plusButton)

  reactions += {
    case ButtonClicked(`plusButton`) => addRow
  }
}

