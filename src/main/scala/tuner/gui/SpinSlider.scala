package tuner.gui

import scala.swing.BoxPanel
import scala.swing.Orientation
import scala.swing.event.ValueChanged

import javax.swing.BoundedRangeModel

class SpinSlider(minv:Float, maxv:Float, numSteps:Int) 
    extends BoxPanel(Orientation.Horizontal) {
  
  val spinner = new Spinner(minv, maxv, numSteps) {
    minimumSize = new java.awt.Dimension(70, 25)
    preferredSize = new java.awt.Dimension(70, 25)
    maximumSize = new java.awt.Dimension(70, 25)
  }
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
    case ValueChanged(`slider`) => 
      spinner.value = slider.floatVal
      publish(new ValueChanged(SpinSlider.this))
    case ValueChanged(`spinner`) => 
      slider.floatVal = spinner.value
      publish(new ValueChanged(SpinSlider.this))
  }

  def value : Float = spinner.value
  def value_=(v:Float) = spinner.value = v

  def minVal : Float = spinner.min
  def minVal_=(v:Float) = spinner.min = v

  def maxVal : Float = spinner.max
  def maxVal_=(v:Float) = spinner.max = v
}

