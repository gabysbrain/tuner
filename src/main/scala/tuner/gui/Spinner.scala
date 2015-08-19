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

  def value : Float = peer.getValue.asInstanceOf[Number].floatValue
  def value_=(v:Float) = peer.setValue(v)

  def min_=(v:Float) = model.setMinimum(float2Float(v))
  def min : Float = model.getMinimum.asInstanceOf[Number].floatValue

  def max_=(v:Float) = model.setMaximum(float2Float(v))
  def max : Float = model.getMaximum.asInstanceOf[Number].floatValue

  override def maximumSize = preferredSize

  peer.addChangeListener(new javax.swing.event.ChangeListener {
    def stateChanged(e:javax.swing.event.ChangeEvent) {
      publish(new ValueChanged(Spinner.this))
    }
  })
}

