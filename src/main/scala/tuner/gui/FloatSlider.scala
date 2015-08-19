package tuner.gui

import scala.swing.Slider

class FloatSlider(minVal:Float, maxVal:Float, numSteps:Int) extends Slider {
  
  val rangeModel = new FloatRangeModel(minVal, maxVal, numSteps)
  peer.setModel(rangeModel)

  def maxFloat : Float = rangeModel.maxFloat
  def maxFloat_=(v:Float) = {rangeModel.maxFloat = v}
  def minFloat : Float = rangeModel.minFloat
  def minFloat_=(v:Float) = {rangeModel.minFloat = v}
  def step : Float = (maxFloat - minFloat) / steps
  def steps = rangeModel.getMaximum
  def steps_=(s:Int) = {
    rangeModel.setMaximum(s)
  }
  def floatVal : Float = rangeModel.minFloat + step * rangeModel.value
  def floatVal_=(v:Float) = 
    rangeModel.setValue(math.round((v-rangeModel.minFloat)/step))

}

