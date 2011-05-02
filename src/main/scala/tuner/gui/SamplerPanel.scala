package tuner.gui

import scala.swing.BoxPanel
import scala.swing.ComboBox
import scala.swing.Orientation
import scala.swing.TablePanel
import scala.swing.TextField

class SamplerPanel extends BoxPanel(Orientation.Vertical) {
  
  val sampleNumField = new TextField
  val sampleTimeField = new TextField
  val ttlRunTimeField = new TextField
  val shapeSelector = new ComboBox(List("Ellipse", "Box"))
  val methodSelector = new ComboBox(List("LHS", "Cartesian"))

  val controlPane = new TablePanel(2, 5) {
    contents += new Label("Number of Samples")
    contents += new Label("x Time per Sample")
    contents += new Label("= Total Run Time")
    contents += shapeSelector
    contents += methodSelector

    contents += sampleNumField
    contents += sampleTimeField
    contents += ttlRunTimeField
    contents += shapeSelector
    contents += methodSelector
  }
}

