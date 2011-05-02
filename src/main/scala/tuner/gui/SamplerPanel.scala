package tuner.gui

import scala.swing.BorderPanel
import scala.swing.BoxPanel
import scala.swing.ComboBox
import scala.swing.GridBagPanel
import scala.swing.Label
import scala.swing.Orientation
import scala.swing.Swing
import scala.swing.TextField

import tuner.Config

class SamplerPanel extends BoxPanel(Orientation.Vertical) {
  
  val sampleNumField = new TextField
  val sampleTimeField = new TextField
  val ttlRunTimeField = new TextField
  val shapeSelector = new ComboBox(List("Ellipse", "Box"))
  val methodSelector = new ComboBox(List("LHS", "Cartesian"))

  val controlPane = new GridBagPanel {
    val c = new Constraints
    c.fill = GridBagPanel.Fill.Horizontal

    c.weightx = 0.0 // Don't change size
    c.anchor = GridBagPanel.Anchor.LineEnd
    c.gridx = 0; c.gridy = 0
    layout(new Label("Number of Samples")) = c

    c.gridx = 0; c.gridy = 1
    layout(new Label("x Time per Sample")) = c

    c.gridx = 0; c.gridy = 2
    layout(new Label("= Total Run Time")) = c

    c.gridx = 0; c.gridy = 3
    layout(new Label("Shape")) = c

    c.gridx = 0; c.gridy = 4
    layout(new Label("Method")) = c

    c.weightx = 1
    c.gridx = 1; c.gridy = 0
    layout(sampleNumField) = c

    c.gridx = 1; c.gridy = 1
    layout(sampleTimeField) = c

    c.gridx = 1; c.gridy = 2
    layout(ttlRunTimeField) = c

    c.gridx = 1; c.gridy = 3
    layout(shapeSelector) = c

    c.gridx = 1; c.gridy = 4
    layout(methodSelector) = c

    border = Swing.TitledBorder(border, "Sampling")
  }

  val splomPanel = new BorderPanel

  contents += controlPane
  contents += splomPanel
}

