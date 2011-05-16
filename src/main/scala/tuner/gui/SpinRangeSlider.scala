package tuner.gui

import scala.swing.BoxPanel
import scala.swing.FloatRangeSlider
import scala.swing.Orientation
import scala.swing.Spinner
import scala.swing.event.ValueChanged

import javax.swing.BoundedRangeModel

class SpinRangeSlider(minVal:Float, maxVal:Float, numSteps:Int) 
    extends BoxPanel(Orientation.Horizontal) {
  
  val slider = new FloatRangeSlider(minVal, maxVal, numSteps)
  val lowSpinner = new Spinner(minVal, maxVal, numSteps)
  val highSpinner = new Spinner(minVal, maxVal, numSteps)

  // Set the initial spinner values
  lowSpinner.value = slider.lowValue
  highSpinner.value = slider.highValue

  listenTo(slider)
  listenTo(lowSpinner)
  listenTo(highSpinner)

  contents += lowSpinner
  contents += slider
  contents += highSpinner

  reactions += {
    case ValueChanged(`slider`) => 
      val (low, high) = slider.value
      lowSpinner.value = low
      highSpinner.value = high
    case ValueChanged(`lowSpinner`) => 
      slider.lowValue = lowSpinner.value
    case ValueChanged(`highSpinner`) => 
      slider.highValue = highSpinner.value
  }

  def value : (Float,Float) = slider.value
}

