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
    val c = new Constraints
    c.fill = GridBagPanel.Fill.Horizontal
    c.weightx = 1.0
    c.anchor = GridBagPanel.Anchor.PageEnd
    c.gridx = i; c.gridy = 0
    layout(new Label(hdr)) = c
  }

  // now set up the first row
  addRow

  protected def addRow = {
    val controls = controlRow

    // We might need to change the plus to a minus button
    if(numRows > 0) {
      val c = new Constraints
      c.weightx = 0.0
      c.anchor = GridBagPanel.Anchor.LineStart
      c.gridx = controls.length; c.gridy = numRows
      layout(new Button("-")) = c
    }

    val row = numRows + 1
    val c = new Constraints
    c.fill = GridBagPanel.Fill.Horizontal
    //c.anchor = GridBagPanel.Anchor.LineStart
    c.weightx = 1.0
    controls.zipWithIndex foreach {case (control, i) =>
      c.gridx = i; c.gridy = row
      layout(control) = c
    }
    c.weightx = 0.0
    c.gridx = controls.length; c.gridy = row
    c.anchor = GridBagPanel.Anchor.LineEnd
    layout(plusButton) = c

    numRows += 1
  }

  listenTo(plusButton)

  reactions += {
    case ButtonClicked(`plusButton`) => addRow
  }
}

