package tuner.gui

import scala.swing.BoxPanel
import scala.swing.FloatSlider
import scala.swing.Orientation
import scala.swing.Spinner
import scala.swing.event.ValueChanged

import javax.swing.BoundedRangeModel

class SpinSlider(minVal:Float, maxVal:Float, numSteps:Int) 
    extends BoxPanel(Orientation.Horizontal) {
  
  val spinner = new Spinner(minVal, maxVal, numSteps)
  val slider = new FloatSlider(minVal, maxVal, numSteps) {
    minFloat = minVal
    maxFloat = maxVal
    steps = numSteps
  }

  listenTo(slider)
  listenTo(spinner)

  contents += slider
  contents += spinner

  reactions += {
    case ValueChanged(`slider`) => spinner.value = slider.floatVal
    case ValueChanged(`spinner`) => slider.floatVal = spinner.value
  }

  def value : Float = spinner.value
}

