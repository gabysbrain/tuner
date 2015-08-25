package tuner.gui

import scala.swing.{Component, Publisher}
import scala.swing.event.ValueChanged

import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

class Spinner(minVal:Float, maxVal:Float, step:Float) 
    extends Component with Publisher {

  def this(minVal:Float, maxVal:Float, steps:Int) = 
    this(minVal, maxVal, (maxVal - minVal) / steps)

  lazy val model = {
    val cur = new java.lang.Double(minVal)
    val mn = new java.lang.Double(minVal)
    val mx = new java.lang.Double(maxVal)
    val s = new java.lang.Double(step)
    new SpinnerNumberModel(cur, mn, mx, s)
  }

  override lazy val peer:JSpinner = new JSpinner(model) with SuperMixin

  // the number model doesn't do bounds checking
  def value : Float = peer.getValue.asInstanceOf[Number].floatValue
  def value_=(v:Float) = {
    val mx = this.max
    val mn = this.min
    peer.setValue(math.min(math.max(this.min, v), this.max))
  }

  // the number model doesn't do bounds checking
  def min_=(v:Float) = {
    model.setMinimum(float2Float(v))
    this.value = this.value // recheck bounds
  }
  def min : Float = model.getMinimum.asInstanceOf[Number].floatValue

  // the number model doesn't do bounds checking
  def max_=(v:Float) = {
    model.setMaximum(float2Float(v))
    this.value = this.value // recheck bounds
  }
  def max : Float = model.getMaximum.asInstanceOf[Number].floatValue

  override def maximumSize = preferredSize

  peer.addChangeListener(new javax.swing.event.ChangeListener {
    def stateChanged(e:javax.swing.event.ChangeEvent) {
      publish(new ValueChanged(Spinner.this))
    }
  })
}

