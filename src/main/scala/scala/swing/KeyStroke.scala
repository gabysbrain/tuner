package scala.swing

import java.awt.event.KeyEvent
import java.awt.event.InputEvent
import javax.swing.{KeyStroke => JKS}

object KeyStroke {
  implicit def ks2KeyStroke(ks:BaseKey) : javax.swing.KeyStroke = ks match {
    case CtrlKey(key) => JKS.getKeyStroke(new java.lang.Character(key), 
                                          InputEvent.CTRL_MASK)
  }

  sealed abstract trait BaseKey
  case class CtrlKey(key:Char) extends BaseKey

}

