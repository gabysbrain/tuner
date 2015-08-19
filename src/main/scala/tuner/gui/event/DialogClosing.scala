package tuner.gui.event

import scala.swing.event.WindowEvent

import scala.swing.Dialog
import scala.swing.Window

case class DialogClosing(override val source:Window, result:Dialog.Result.Value) extends WindowEvent(source) 

