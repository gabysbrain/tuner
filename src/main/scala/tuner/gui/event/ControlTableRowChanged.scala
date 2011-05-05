package tuner.gui.event

import scala.swing.Component
import scala.swing.event.ComponentEvent

case class ControlTableRowChanged(source:Component, row:List[Component]) 
    extends ComponentEvent {
}

