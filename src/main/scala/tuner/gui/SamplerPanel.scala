package tuner.gui

import scala.swing.BorderPanel
import scala.swing.BoxPanel
import scala.swing.ComboBox
import scala.swing.TablePanel
import scala.swing.Label
import scala.swing.Orientation
import scala.swing.Swing
import scala.swing.TextField
import scala.swing.event.SelectionChanged
import scala.swing.event.ValueChanged

import tuner.Config
import tuner.Sampler

class SamplerPanel(project:tuner.project.Sampler) 
    extends BoxPanel(Orientation.Vertical) {
  
  val sampleNumField = new TextField
  val sampleTimeField = new TextField
  val ttlRunTimeField = new TextField
  val shapeSelector = new RegionShapeCombo
  val methodSelector = new ComboBox(List("LHS", "Random"))

  val controlPane = new TablePanel(2, 2) {
    // Labels in left column
    layout(new Label("Number of Samples")) = (0,0)
    //layout(new Label("x Time per Sample")) = (0,1)
    //layout(new Label("= Total Run Time")) = (0,2)
    //layout(new Label("Shape")) = (0,3)
    layout(new Label("Method")) = (0,1)

    // Fields in left column
    layout(sampleNumField) = (1,0)
    //layout(sampleTimeField) = (1,1)
    //layout(ttlRunTimeField) = (1,2)
    //layout(shapeSelector) = (1,3)
    layout(methodSelector) = (1,1)

    border = Swing.TitledBorder(border, "Sampling")
  }

  val splomPanel = new SamplerSplomPanel(project)

  contents += controlPane
  contents += splomPanel

  // Set up the events
  listenTo(sampleNumField)
  //listenTo(shapeSelector)
  listenTo(methodSelector.selection)

  var lastSamples:Int = numSamples
  var lastSelection:String = methodString

  reactions += {
    case ValueChanged(`sampleNumField`) | SelectionChanged(`methodSelector`) => 
      // Only publish if something actually changed
      if(lastSamples != numSamples || lastSelection != methodString) {
        lastSamples = numSamples
        lastSelection = methodString
        publish(new ValueChanged(this))
      }
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
}

