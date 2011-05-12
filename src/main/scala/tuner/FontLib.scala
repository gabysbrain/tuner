
import processing.core.PApplet
import processing.core.PFont

object FontLib {
  val fontPath = "fonts/Maven Pro.otf"
  var applet:PApplet = null
  private var loadedFonts = Map[Int,PFont]()

  def loadSize(size:Int) : PFont = applet.createFont(fontPath, size)
  def font(size:Int) : PFont = {
    loadedFonts.getOrElse(size, {
      val f = loadSize(size)
      loadedFonts += size -> f
      f
    })
  }
}

