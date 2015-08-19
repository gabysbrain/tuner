package tuner.gui

import scala.swing.Action

import KeyStroke._

case class CopyAction(title0:String) extends Action(title0) {
  override lazy val peer = new javax.swing.text.DefaultEditorKit.CopyAction()
  title = title0
  accelerator = Some(CommandKey('C'))
  def apply = {}
}

case class CutAction(title0:String) extends Action(title0) {
  override lazy val peer = new javax.swing.text.DefaultEditorKit.CutAction()
  title = title0
  accelerator = Some(CommandKey('X'))
  def apply = {}
}

case class PasteAction(title0:String) extends Action(title0) {
  override lazy val peer = new javax.swing.text.DefaultEditorKit.PasteAction()
  title = title0
  accelerator = Some(CommandKey('V'))
  def apply = {}
}

