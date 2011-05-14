package tuner.gui

import scala.swing.BoxPanel
import scala.swing.FloatSlider
import scala.swing.Orientation
import scala.swing.Spinner

import javax.swing.BoundedRangeModel

class SpinSlider(minVal:Float, maxVal:Float, steps:Int) 
    extends BoxPanel(Orientation.Horizontal) {
  
  val spinner = new Spinner(minVal, maxVal, steps)
  val slider = new FloatSlider

  contents += slider
  contents += spinner

  def value : Float = spinner.value
}

