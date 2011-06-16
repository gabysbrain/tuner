package tuner.gui.event

import scala.swing.Component
import scala.swing.event.ComponentEvent

case class ReadyToDraw(source:Component) extends ComponentEvent

