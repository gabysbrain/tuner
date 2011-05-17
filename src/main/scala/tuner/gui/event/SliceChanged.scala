package tuner.gui.event

import scala.swing.Component
import scala.swing.event.ComponentEvent

case class SliceChanged(source:Component, newVals:List[(String,Float)]) 
  extends ComponentEvent

