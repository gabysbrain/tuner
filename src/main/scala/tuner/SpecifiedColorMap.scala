package tuner

import tuner.gui.P5Panel

class SpecifiedColorMap(cm:ColorMap, mnv:Float, mxv:Float) {
  
  private var _minVal:Float = mnv
  var maxVal:Float = mxv
  private var _filterVal:Float = mnv

  def minVal = _minVal
  def minVal_=(v:Float) = {
    _minVal = v
    _filterVal = P5Panel.constrain(_filterVal, minVal, maxVal)
  }
  def filterVal = _filterVal
  def filterVal_=(v:Float) = {
    _filterVal = P5Panel.constrain(v, minVal, maxVal)
  }

  def colors = cm.colors
  def breaks = cm.breaks

  def color(v:Float) : Int = {
    val constVal = P5Panel.constrain(v, minVal, maxVal)
    if(constVal < filterVal) {
      if(colors.head < 128) 255
      else                  0
    } else {
      val pct = P5Panel.norm(constVal, filterVal, maxVal)
      cm.lerp(pct)
    }
  }
}

