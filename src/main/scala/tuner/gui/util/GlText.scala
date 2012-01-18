package tuner.gui.util

import com.jogamp.common.nio.Buffers
import javax.media.opengl.{GL,GL2}
import javax.media.opengl.GLAutoDrawable

import processing.core.PFont

object GlText {

  // Types of horiz alignment
  sealed trait VAlign
  case object Top extends VAlign
  case object VCenter extends VAlign
  case object Bottom extends VAlign

  // Types of vertical alignment
  sealed trait HAlign
  case object Left extends HAlign
  case object HCenter extends HAlign
  case object Right extends HAlign

  def drawString(drawable:GLAutoDrawable, text:String, 
                 font:PFont, size:Float,
                 x:Float, y:Float,
                 vAlign:VAlign, hAlign:HAlign) = {
    val txt = new GlText(text, font, size)
    txt.vAlign = vAlign
    txt.hAlign = hAlign
    txt.draw(drawable, x, y)
  }

  def width(text:String, font:PFont, size:Float) = 
    new GlText(text, font, size).width

}

/**
 * Font size is in coordinate system size
 *
 * This class is mostly from the Processing implementation
 */
class GlText(text:String, font:PFont, size:Float) {

  var vAlign:GlText.VAlign = GlText.VCenter
  var hAlign:GlText.HAlign = GlText.HCenter

  def draw(drawable:GLAutoDrawable, x:Float, y:Float) : Unit = {
    val gl = drawable.getGL

    // Borrowed from processing source code
    val leading = 1.275f * (font.ascent + font.descent)

    // Figure out exactly where to start drawing
    val startY = vAlign match {
      case GlText.Top    => y + font.ascent
      case GlText.VCenter => y + font.ascent / 2
      case GlText.Bottom => y - font.descent
    }
    val startX = hAlign match {
      case GlText.Left   => x
      case GlText.HCenter => x + width / 2
      case GlText.Right  => x - width
    }

    // reuse the texture memory
    val texId = genTexture(drawable.getGL.getGL2)

    // Draw the string one letter at a time
    text.foldLeft(startX) {(xx,c) =>
      xx + drawChar(drawable, c, xx, startY, texId)
    }

    // No more textures
    gl.glDeleteTextures(1, Array(texId), 0)
  }

  def drawChar(drawable:GLAutoDrawable, c:Char,
               x:Float, y:Float, textureId:Int) = {
    val gl = drawable.getGL.getGL2

    val glyph = font.getGlyph(c)

    // Figure out the dimensions of the quad
    /*
    val high    = glyph.height     / font.size.toFloat
    val bwidth  = glyph.width      / font.size.toFloat
    val lextent = glyph.leftExtent / font.size.toFloat
    val textent = glyph.topExtent  / font.size.toFloat
    */
    val high    = glyph.height
    val bwidth  = glyph.width
    val lextent = glyph.leftExtent
    val textent = glyph.topExtent

    val x1 = x + lextent * size
    val y1 = y - textent * size
    val x2 = x1 + bwidth * size
    val y2 = y1 + high * size

    // Assign the glyph to the texture
    val img = glyph.image
    img.loadPixels
    val imgBuf = Buffers.newDirectIntBuffer(img.pixels)
    gl.glBindTexture(GL.GL_TEXTURE_2D, textureId)
    gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGB8, 
                    img.width, img.height, 
                    0, GL.GL_RGBA, 
                    GL.GL_UNSIGNED_INT, imgBuf)
    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR)
    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR)

    // Finally!  draw the texture mapped quad
    gl.glBegin(GL2.GL_QUADS)
    gl.glTexCoord2f(0f, 0f); gl.glVertex3f(x1, y1, 0f)
    gl.glTexCoord2f(1f, 0f); gl.glVertex3f(x2, y1, 0f)
    gl.glTexCoord2f(1f, 1f); gl.glVertex3f(x2, y2, 0f)
    gl.glTexCoord2f(0f, 1f); gl.glVertex3f(x1, y2, 0f)
    gl.glEnd

    glyph.width * size
  }

  def width = text.map {c => font.width(c) * size} reduceLeft(_ + _)

  private def genTexture(gl:GL2) : Int = {
    val texId = Array(0)
    gl.glGenTextures(1, texId, 0)
    gl.glBindTexture(GL.GL_TEXTURE_2D, texId(0))
    texId(0)
  }
}

