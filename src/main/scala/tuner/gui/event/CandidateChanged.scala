package tuner.gui.event

import scala.swing.Component
import scala.swing.event.ComponentEvent

case class CandidateChanged(source:Component, newCand:List[(String,Float)]) 
  extends ComponentEvent

