package tuner.gui

import scala.swing.Panel
import scala.swing.SequentialContainer

import org.japura.gui.CollapsibleRootPanel

object CollapsiblePanel {
  sealed trait Mode {
    val value : CollapsibleRootPanel.Mode
  }
  case object Fill extends Mode {
    val value = CollapsibleRootPanel.FILL
  }
  case object Scroll extends Mode {
    val value = CollapsibleRootPanel.SCROLL_BAR
  }
}
class CollapsiblePanel(m:CollapsiblePanel.Mode) 
    extends Panel with SequentialContainer.Wrapper {
  override lazy val peer = new CollapsibleRootPanel(m.value) with SuperMixin

  /*
  def collapsed : Boolean = peer.isCollapsed
  def collapsed_=(b:Boolean) = {
    if(b) peer.collapse
    else  peer.expand
  }
  */
}

