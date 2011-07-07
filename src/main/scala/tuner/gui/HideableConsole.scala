package tuner.gui

import scala.swing.BorderPanel
import scala.swing.CheckBox
import scala.swing.ScrollPane
import scala.swing.TextArea
import scala.swing.event.ButtonClicked
import scala.swing.event.UIElementResized

class HideableConsole extends BorderPanel {
  val hideButton = new CheckBox("Show Console") {
    selected = false
  }
  val console = new TextArea(20, 50) {
    editable = false
  }

  listenTo(hideButton)

  val scrollConsole = new ScrollPane {
    contents = console
    visible = hideButton.selected
  }
  layout(hideButton) = BorderPanel.Position.North
  layout(scrollConsole) = BorderPanel.Position.Center

  reactions += {
    case ButtonClicked(`hideButton`) =>
      scrollConsole.visible = hideButton.selected
      publish(UIElementResized(this))
  }

  def text_=(t:String) = console.text = t
  def text = console.text
}

