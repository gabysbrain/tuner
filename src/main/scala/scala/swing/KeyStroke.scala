package scala.swing

import java.awt.event.KeyEvent
import java.awt.event.InputEvent
import javax.swing.{KeyStroke => JKS}

object KeyStroke {
  implicit def ks2KeyStroke(ks:BaseKey) : javax.swing.KeyStroke = 
    JKS.getKeyStroke(ks.character, ks.mask)

  sealed abstract trait BaseKey {
    val mask = 0
    def character : java.lang.Character
  }
  case class CtrlKey(key:Char) extends BaseKey {
    override val mask = InputEvent.CTRL_MASK
    def character : java.lang.Character = new java.lang.Character(key)
  }
  case class CommandKey(key:Char) extends BaseKey {
    override val mask = java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    def character : java.lang.Character = new java.lang.Character(key)
  }
  case class ShiftCommandKey(key:Char) extends BaseKey {
    override val mask = java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | InputEvent.SHIFT_MASK;
    def character : java.lang.Character = new java.lang.Character(key)
  }
  
  private def isMac : Boolean = {
    System.getProperty("os.name").toLowerCase.indexOf("mac") >= 0
  }
}

