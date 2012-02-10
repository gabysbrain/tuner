package tuner.gui.event

import scala.swing.Component
import scala.swing.event.ComponentEvent

case class ViewChanged(source:Component) extends ComponentEvent

