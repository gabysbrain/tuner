package tuner.gui

import com.tomtorsneyweir.datescala.swing.TimeField

import scala.swing.ComboBox
import scala.swing.Label
import scala.swing.TablePanel
import scala.swing.TextField
import scala.swing.event.SelectionChanged
import scala.swing.event.ValueChanged

import tuner.Sampler

/**
 * A panel allowing either the user to select different methods
 * of generating samples from within Tuner
 */
class SampleGenerationPanel(newSamples:((Int, Sampler.Method) => Unit))
    extends TablePanel(2,4) {
  // regression parameters for later
  /*
  val tf = Array(7.001e-09, 6.855e-09, 7.759e-09, 7.690e-09, 7.656e-09, 7.629e-09)
  val m1 = Array(-25.491, -8.099, -4.338, -1.917, -1.988, -2.354)
  val m2 = Array(35.093, 15.390, 9.720, 5.239, 5.332, 6.459)
  val m3 = Array(-20.368, -13.183, -10.283, -7.462, -7.288, -8.111)
  val m4 = Array(16.51, 16.50, 16.68, 16.65, 17.02, 17.55)
  val b1 = Array(-2.849e-05, -1.924e-05, -1.239e-05, -8.960e-06, -6.961e-06, -6.542e-06)
  val b2 = Array(2.134e-05, 1.924e-05, 1.594e-05, 1.416e-05, 1.304e-05, 1.445e-05)
  val b3 = Array(-3.981e-06, -5.894e-06, -6.588e-06, -7.323e-06, -8.048e-06, -1.053e-05)
  val b4 = Array(2.257e-07, 5.635e-07, 8.706e-07, 1.228e-06, 1.622e-06, 2.511e-06)
  */

  val sampleNumField = new TextField
  val sampleTimeField = new TimeField
  val ttlRunTimeField = new TimeField
  //val maxFpsField = new TimeField
  val shapeSelector = new RegionShapeCombo
  val methodSelector = new ComboBox(List("LHS", "Random"))

  // Labels in left column
  layout(new Label("Number of Samples")) = (0,0)
  //layout(new Label("Frames per Second")) = (0,1)
  layout(new Label("x Time per Sample")) = (0,1)
  layout(new Label("= Total Run Time")) = (0,2)
  layout(new Label("Method")) = (0,3)

  // Fields in left column
  layout(sampleNumField) = (1,0)
  //layout(maxFpsField) = (1,1)
  layout(sampleTimeField) = (1,1)
  layout(ttlRunTimeField) = (1,2)
  layout(methodSelector) = (1,3)

  // Set up the events
  listenTo(sampleNumField)
  listenTo(sampleTimeField)
  listenTo(ttlRunTimeField)
  //listenTo(maxFpsField)
  listenTo(methodSelector.selection)

  var lastSamples:Int = numSamples
  var lastSampleTime:Option[Long] = sampleTimeField.millis
  var lastTotalTime:Option[Long] = ttlRunTimeField.millis
  var lastSelection:String = methodString

  // Keep track of all the text field update times
  var sampleNumModTime:Long = System.currentTimeMillis
  var sampleTimeModTime:Long = System.currentTimeMillis
  var ttlTimeModTime:Long = System.currentTimeMillis

  var internalChange:Boolean = false
  
  reactions += {
    case SelectionChanged(`methodSelector`) => 
      handleSamplesChanged
    case ValueChanged(`sampleNumField`) =>
      handleSamplesChanged
      if(!internalChange) {
        sampleNumModTime = System.currentTimeMillis
        internalChange = true
        if(sampleTimeModTime > ttlTimeModTime)
          updateTotalTime
        else
          updateSampleTime
        internalChange = false
      }
    case ValueChanged(`sampleTimeField`) =>
      if(lastSampleTime != sampleTimeField.millis) {
        lastSampleTime = sampleTimeField.millis
        if(!internalChange) {
          sampleTimeModTime = System.currentTimeMillis
          internalChange = true
          if(sampleNumModTime > ttlTimeModTime)
          updateTotalTime
            else
            updateNumSamples
          internalChange = false
        }
      }
    case ValueChanged(`ttlRunTimeField`) => 
      if(lastTotalTime != ttlRunTimeField.millis) {
        lastTotalTime = ttlRunTimeField.millis
        if(!internalChange) {
          ttlTimeModTime = System.currentTimeMillis
          internalChange = true
          if(sampleNumModTime > sampleTimeModTime)
            updateSampleTime
          else
            updateNumSamples
          internalChange = false
        }
      }
      //updateMaxFps
    //case ValueChanged(`maxFpsField`) =>
      //updateSampleNum
  }

  def numSamples : Int = {
    try {
      sampleNumField.text.toInt
    } catch {
      case nfe:NumberFormatException => 0
    }
  }

  //def shape : String = shapeSelector.toString

  def methodString = methodSelector.selection.item

  def method : Sampler.Method = {
    //println(methodSelector.selection.item)
    methodSelector.selection.item match {
      case "LHS" => Sampler.lhc
      case "Random" => Sampler.random
    }
  }

  private def handleSamplesChanged = {
    // Only publish if something actually changed
    if(lastSamples != numSamples || lastSelection != methodString) {
      lastSamples = numSamples
      lastSelection = methodString
      newSamples(numSamples, method)
      publish(new ValueChanged(this))
    }
  }

  private def updateNumSamples = {
    (sampleTimeField.millis, ttlRunTimeField.millis) match {
      case (Some(st), Some(tt)) if(st > 0) => 
        sampleNumField.text = (tt/st).toInt.toString
      case _ =>
    }
  }

  /*
  private def updateMaxFps = {
    deafTo(maxFpsField)
    val N = List(lastSamples, numSamples).max
    val rr = if(N==0) 1f
             else     1f / (2f*N)
    val ft = N * onePtTime(rr)
    maxFpsField.text = if(ft > 0) (1 / ft).toString
                       else       ""
    listenTo(maxFpsField)
  }

  private def updateSampleNum = {
    deafTo(sampleNumField)
    val N = List(lastSamples, numSamples).max
    val rr = if(N == 0) 1f
             else       1f / (2f*N)
    try {
      val fps = maxFpsField.text.toFloat
      val numSamp = (1/(fps * onePtTime(rr))).toInt
      sampleNumField.text = if(numSamp > 0) numSamp.toString
                            else            ""
    } catch {
      case nfe:NumberFormatException => 
        sampleNumField.text = ""
    }
    handleSamplesChanged
    listenTo(sampleNumField)
  }
  */

  private def updateSampleTime = {
    (ttlRunTimeField.millis, numSamples > 0) match {
      case (Some(tt), true) => sampleTimeField.millis = tt / numSamples
      case _ =>
    }
  }

  private def updateTotalTime = {
    (sampleTimeField.millis, numSamples > 0) match {
      case (Some(st), true) => ttlRunTimeField.millis = numSamples * st
      case _ =>
    }
  }

  /*
  private def onePtTime(r:Float) : Float = {
    val d = project.sampleRanges.length
    val m = math.exp(-(m1(d-3)*math.pow(r,3) + m2(d-3)*math.pow(r,2) + 
                       m3(d-3)*r + m4(d-3)))
    val b = b1(d-3)*math.pow(r,3) + b2(d-3)*math.pow(r,2) + b3(d-3)*r + b4(d-3)
    val mytf = tf(d-3)
    val panels = d*(d-1) / 2
    panels.toFloat * (mytf + m * expPts(d, r) + b).toFloat
  }

  private def expPts(d:Int, r:Float) : Float = {
    val n = d - 2
    if(n == 1) {
      (2 * r - math.pow(r, 2)).toFloat
    } else {
      var term1:Float = 0f
      for(i <- 1 until n) {
        var pr = 1f
        for(k <- 0 until (i+1))
          pr += n + i - 2*k
        var num = math.pow(2, i+1) * 
                  math.pow(math.Pi, (n-i).toFloat/2f) * 
                  math.pow(r, n+i)
        var den = gamma((n-1).toFloat/2f) * pr
        term1 += (math.pow(-1, i) * choose(n, i) * num / den).toFloat
      }
      val term2:Float = math.pow(-1, n).toFloat * math.pow(r, 2*n).toFloat / 
                        factorial(n)
      val term3:Float = nsphereVol(d, r)
      term1 + term2 + term3
    }
  }

  private def nsphereVol(d:Int, r:Float) : Float = {
    if(d == 1) {
      2 * r
    } else {
      (math.pow(math.Pi, d.toFloat/2f) * math.pow(r, d) / 
        gamma(d.toFloat/2f + 1f)).toFloat
    }
  }

  val choose = org.apache.commons.math.util.MathUtils.binomialCoefficient _
  val factorial = org.apache.commons.math.util.MathUtils.factorial _
  def gamma(x:Double) : Double = 
    math.exp(org.apache.commons.math.special.Gamma.logGamma(x))
  */
}

