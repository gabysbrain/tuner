package tuner.gui

import scala.swing.BorderPanel
import scala.swing.CheckBox
import scala.swing.TextArea
import scala.swing.event.ButtonClicked

class HideableConsole extends BorderPanel {
  val hideButton = new CheckBox("Show Console") {
    selected = false
  }
  val console = new TextArea(20, 50) {
    editable = false
  }

  console.visible = hideButton.selected

  listenTo(hideButton)

  layout(hideButton) = BorderPanel.Position.North
  layout(console) = BorderPanel.Position.Center

  reactions += {
    case ButtonClicked(`hideButton`) =>
      console.visible = hideButton.selected
  }

  def text_=(t:String) = console.text = t
  def text = console.text
}

