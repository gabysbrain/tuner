package tuner

import tuner.util.AxisTicks
import tuner.gui.P5Panel

class SpecifiedColorMap(cm:ColorMap, mnv:Float, mxv:Float, invert:Boolean) {
  
  private var _ticks = AxisTicks.ticks(mnv, mxv, Config.colorbarTicks)
  private var _filterVal:Float = if(invert) _ticks.last else _ticks.head

  def minVal = _ticks.head
  def minVal_=(v:Float) = {
    _ticks = AxisTicks.ticks(v, maxVal, Config.colorbarTicks)
    _filterVal = P5Panel.constrain(_filterVal, minVal, maxVal)
  }
  def filterVal = _filterVal
  def filterVal_=(v:Float) = {
    _filterVal = P5Panel.constrain(v, minVal, maxVal)
  }
  def maxVal = _ticks.last
  def maxVal_=(v:Float) = {
    _ticks = AxisTicks.ticks(minVal, v, Config.colorbarTicks)
    _filterVal = P5Panel.constrain(v, minVal, maxVal)
  }

  def isInverted = invert
  def isFiltered = if(isInverted) {
    filterVal < maxVal
  } else {
    filterVal > minVal
  }
  def colors = cm.colors
  def ticks = _ticks

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

