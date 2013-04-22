package tuner.gui.util

import java.awt.Graphics2D

object TextAlign {
  sealed trait TextVAlign
  case object Top extends TextVAlign
  case object Middle extends TextVAlign
  case object Bottom extends TextVAlign

  sealed trait TextHAlign
  case object Left extends TextHAlign
  case object Center extends TextHAlign
  case object Right extends TextHAlign
}

object FontLib {

  import TextAlign._

  def textWidth(g:Graphics2D, str:String) = {
    val metrics = g.getFontMetrics(g.getFont)
    metrics.stringWidth(str)
  }

  def drawString(g:Graphics2D, str:String, x:Int, y:Int, 
                 hAlign:TextHAlign, vAlign:TextVAlign) = {
    val metrics = g.getFontMetrics(g.getFont)
    val height = metrics.getAscent
    val width = metrics.stringWidth(str)

    val xx = hAlign match {
      case Left   => x
      case Center => x - (width / 2)
      case Right  => x - width
    }
    val yy = vAlign match {
      case Top    => y + height
      case Middle => y + (height / 2)
      case Bottom => y 
    }

    g.drawString(str, xx, yy)
  }

  def drawVString(g:Graphics2D, str:String, x:Int, y:Int,
                  hAlign:TextHAlign, vAlign:TextVAlign) = {

    // Need to compute these before rotation
    val metrics = g.getFontMetrics(g.getFont)
    val height = metrics.getAscent
    val width = metrics.stringWidth(str)

    val xx = hAlign match {
      case Left   => x + height
      case Center => x + (height / 2)
      case Right  => x
    }
    val yy = vAlign match {
      case Top    => y
      case Middle => y + (width / 2)
      case Bottom => y + width
    }

    val oldFont = g.getFont
    val rotFont = oldFont.deriveFont(
      java.awt.geom.AffineTransform.getRotateInstance(-3.1415926 / 2.0))
    g.setFont(rotFont)
    
    g.drawString(str, xx, yy)

    g.setFont(oldFont)
  }

}

