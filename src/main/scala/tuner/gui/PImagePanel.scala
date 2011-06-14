package tuner.gui

import processing.core.PImage

import tuner.Config

class PImagePanel(w:Int, h:Int) extends P5Panel(w, h) {
  
  private var _image:Option[PImage] = None

  def image:Option[PImage] = _image
  def image_=(i:Option[PImage]) : Unit = {
    _image = i
    loop
  }
  def image_=(i:PImage) : Unit = {
    image = Some(i)
  }

  override def setup = {
    noLoop
  }

  def draw = {
    noLoop

    applet.background(Config.backgroundColor)
    
    image.foreach {img =>
      val (imgWidth, imgHeight) = imageSize(img)
      imageMode(P5Panel.ImageMode.Center)
      image(img, w/2, h/2, imgWidth/2, imgHeight/2)
    }
  }

  def imageSize(img:PImage) : (Float,Float) = {
    val maxWidth = math.min(img.width, width)
    val maxHeight = math.min(img.height, height)

    // make sure to maintain aspect ratio when resizing
    val widthDiff = img.width - maxWidth
    val heightDiff = img.height - maxHeight

    // Figure out the most constrained factor and resize by that
    if(widthDiff > heightDiff) {
      (maxWidth, img.height * (maxWidth/img.width))
    } else {
      (img.width * (maxHeight/img.height), maxHeight)
    }
  }
}

