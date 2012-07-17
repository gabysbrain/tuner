package tuner

import tuner.gui.P5Panel

class SpecifiedColorMap(cm:ColorMap, mnv:Float, mxv:Float, invert:Boolean) {
  
  private var _minVal:Float = mnv
  private var _maxVal:Float = mxv
  private var _filterVal:Float = if(invert) mxv else mnv

  def minVal = _minVal
  def minVal_=(v:Float) = {
    _minVal = v
    _filterVal = P5Panel.constrain(_filterVal, minVal, maxVal)
  }
  def filterVal = _filterVal
  def filterVal_=(v:Float) = {
    _filterVal = P5Panel.constrain(v, minVal, maxVal)
  }
  def maxVal = _maxVal
  def maxVal_=(v:Float) = {
    _maxVal = v
    _filterVal = P5Panel.constrain(v, minVal, maxVal)
  }

  def isInverted = invert
  def isFiltered = if(isInverted) {
    filterVal < maxVal
  } else {
    filterVal > minVal
  }
  def colors = cm.colors
  def breaks = {
    List(filterVal, maxVal)
  }

  def filterColor = Config.filterColor
  def minColor = cm.colors.head
  def maxColor = cm.colors.last

  /**
   * The filter goes from filterStart to filterVal
   */
  def filterStart = if(invert) maxVal else minVal

  /**
   * The unfiltered bit goes from filterVal to colorEnd
   */
  def colorEnd = if(invert) minVal else maxVal

  def color(v:Float) : Color = {
    val cv = P5Panel.constrain(v, minVal, maxVal)
    if(!invert && cv < filterVal) {
      Config.filterColor
    } else if(invert && cv > filterVal) {
      Config.filterColor
    } else {
      val pct = if(invert) {
        P5Panel.norm(cv, filterVal, minVal)
      } else {
        P5Panel.norm(cv, filterVal, maxVal)
      }
      cm.lerp(pct)
    }
  }
}

