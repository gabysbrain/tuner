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
      publish(new ValueChanged(SpinRangeSlider.this))
    case ValueChanged(`lowSpinner`) => 
      slider.lowValue = lowSpinner.value
      publish(new ValueChanged(SpinRangeSlider.this))
    case ValueChanged(`highSpinner`) => 
      slider.highValue = highSpinner.value
      publish(new ValueChanged(SpinRangeSlider.this))
  }

  def value : (Float,Float) = slider.value
  def value_=(v:(Float,Float)) = {
    lowSpinner.value = v._1
    highSpinner.value = v._2
  }
  def lowValue : Float = slider.lowValue
  def highValue : Float = slider.highValue
}

