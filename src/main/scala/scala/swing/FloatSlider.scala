package scala.swing

import javax.swing.BoundedRangeModel
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.event.EventListenerList

class FloatSlider extends Slider {
  
  var _max = 1f
  var _min = 0f
  var steps = 100

  val rangeModel = new BoundedRangeModel {
    var _isAdj = false
    var _currentStep = 0

    var listeners:List[ChangeListener] = Nil
  
    def getMinimum : Int = 0
    def getMaximum : Int = steps
    def getExtent : Int = 0
    def getValueIsAdjusting : Boolean = _isAdj
    def setMinimum(m:Int) = {}
    def setExtent(e:Int) = {}
    def setMaximum(m:Int) = {
      val oldMax = max
      steps = m
      if(max != oldMax)
        fireStateChanged
    }
    def setValueIsAdjusting(b:Boolean) = {
      val old = _isAdj
      _isAdj = b
      if(old != _isAdj)
        fireStateChanged
    }

    def setValue(v:Int) = {
      _currentStep = v
    }
    def getValue : Int = _currentStep
    def value = getValue

    def setRangeProperties(newVal:Int, newExtent:Int, 
                           newMin:Int, newMax:Int, 
                           newAdj:Boolean) = {
      var hasChanged = false
      if(newVal != _currentStep)
        hasChanged = true
      if(newAdj != _isAdj)
        hasChanged = true
      if(steps != newMax)
        hasChanged = true
      _currentStep = newVal
      _isAdj = newAdj
      steps = newMax

      if(hasChanged)
        fireStateChanged
    }

    def addChangeListener(l:ChangeListener) = 
      listeners = l::listeners
    def removeChangeListener(l:ChangeListener) = 
      listeners = listeners.diff(List(l))
    
    def fireStateChanged = {
      val evt = new ChangeEvent(this)
      listeners.foreach {l => l.stateChanged(evt)}
    }
  }

  peer.setModel(rangeModel)

  def maxFloat : Float = _max
  def minFloat : Float = _min
  def step : Float = (max - min) / steps
  def floatVal : Float = step * rangeModel.value

}

