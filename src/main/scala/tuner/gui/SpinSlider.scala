package tuner.gui

import scala.swing.BoxPanel
import scala.swing.Orientation
import scala.swing.event.ValueChanged

import javax.swing.BoundedRangeModel

class SpinSlider(minVal:Float, maxVal:Float, numSteps:Int) 
    extends BoxPanel(Orientation.Horizontal) {
  
  val spinner = new Spinner(minVal, maxVal, numSteps) {
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
}

