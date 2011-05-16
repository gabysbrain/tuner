package scala.swing

import prefuse.util.ui.JRangeSlider

class FloatRangeSlider(minVal:Float, maxVal:Float, numSteps:Int) 
    extends Component with Publisher {

  var minFloat = minVal
  var maxFloat = maxVal

  lazy override val peer:JRangeSlider = 
    new JRangeSlider(0, numSteps, 0, numSteps, JRangeSlider.HORIZONTAL)
  
  def steps = peer.getMaximum
  def step : Float = (maxFloat - minFloat) / steps
  def value : (Float,Float) = {
    val lowerVal = minFloat + step * peer.getLowValue
    val upperVal = minFloat + step * peer.getHighValue
    (lowerVal, upperVal)
  }
}

