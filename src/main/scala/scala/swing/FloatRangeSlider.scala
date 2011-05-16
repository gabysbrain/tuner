package scala.swing

import scala.swing.event.ValueChanged

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
  def lowValue : Float = value._1
  def lowValue_=(v:Float) = {
    val intVal = math.round((v - minFloat)/step)
    peer.setLowValue(intVal)
  }
  def highValue : Float = value._2
  def highValue_=(v:Float) = {
    val intVal = math.round((v - minFloat)/step)
    peer.setHighValue(intVal)
  }

  peer.addChangeListener(new javax.swing.event.ChangeListener {
    def stateChanged(e:javax.swing.event.ChangeEvent) {
      publish(new ValueChanged(FloatRangeSlider.this))
    }
  })
}

