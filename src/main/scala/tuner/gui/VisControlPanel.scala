package tuner.gui

import scala.swing.BoxPanel
import scala.swing.ButtonGroup
import scala.swing.CheckBox
import scala.swing.Orientation
import scala.swing.RadioButton
import scala.swing.Swing
import scala.swing.TabbedPane
import scala.swing.event.ButtonClicked
import scala.swing.event.SelectionChanged

import java.awt.Dimension

import tuner.ViewInfo
import tuner.gui.event.ViewChanged

class VisControlPanel(viewInfo:ViewInfo) extends TabbedPane {
  
  val dims = new Dimension(Int.MaxValue, 206)
  preferredSize = dims
  maximumSize = dims

  // ===== Hyperslice controls =====
  val mainResponseButton = new RadioButton("Value")
  val errResponseButton = new RadioButton("Error")
  val gainResponseButton = new RadioButton("Gain")

  val gradientGlyphButton = new CheckBox("Gradient")
  val regionGlyphButton = new CheckBox("Region") {
    selected = viewInfo.showRegion
  }
  val sampleLineGlyphButton = new CheckBox("Line to Sample") {
    selected = viewInfo.showSampleLine
  }

  new ButtonGroup(mainResponseButton, errResponseButton, gainResponseButton)

  val hypersliceTab = new BoxPanel(Orientation.Vertical) {
    contents += new BoxPanel(Orientation.Horizontal) {
      contents += Swing.HGlue
      contents += mainResponseButton
      contents += Swing.HGlue
      contents += errResponseButton
      contents += Swing.HGlue
      contents += gainResponseButton
      contents += Swing.HGlue

      border = Swing.TitledBorder(border, "View")
      preferredSize = dims
      maximumSize = dims
    }
    contents += new BoxPanel(Orientation.Horizontal) {
      //contents += Swing.HGlue
      //contents += gradientGlyphButton
      contents += Swing.HGlue
      contents += regionGlyphButton
      contents += Swing.HGlue
      contents += sampleLineGlyphButton
      contents += Swing.HGlue

      border = Swing.TitledBorder(border, "Glyphs")
      preferredSize = dims
      maximumSize = dims
    }
  }

  // ===== SPLOM controls =====
  val splomTab = new BoxPanel(Orientation.Vertical)

  // Add the tabs
  pages += new TabbedPane.Page("Hyperslice", hypersliceTab)
  pages += new TabbedPane.Page("SPLOM", splomTab)

  // Set up the events
  listenTo(mainResponseButton)
  listenTo(errResponseButton)
  listenTo(gainResponseButton)
  listenTo(regionGlyphButton)
  listenTo(sampleLineGlyphButton)
  listenTo(this.selection)
  
  val me = this
  reactions += {
    case ButtonClicked(`mainResponseButton`) =>
      viewInfo.currentMetric = ViewInfo.ValueMetric
      publish(new ViewChanged(this))
    case ButtonClicked(`errResponseButton`) =>
      viewInfo.currentMetric = ViewInfo.ErrorMetric
      publish(new ViewChanged(this))
    case ButtonClicked(`gainResponseButton`) =>
      viewInfo.currentMetric = ViewInfo.GainMetric
      publish(new ViewChanged(this))
    case ButtonClicked(`sampleLineGlyphButton`) =>
      viewInfo.showSampleLine = sampleLineGlyphButton.selected
      publish(new ViewChanged(this))
    case ButtonClicked(`regionGlyphButton`) =>
      viewInfo.showRegion = regionGlyphButton.selected
      publish(new ViewChanged(this))
    case SelectionChanged(`me`) =>
      this.selection.page.content match {
        case `hypersliceTab` => viewInfo.currentVis = ViewInfo.Hyperslice
        case `splomTab` => viewInfo.currentVis = ViewInfo.Splom
      }
      publish(new ViewChanged(this))
  }

  // Update which metric we're looking at
  viewInfo.currentMetric match {
    case ViewInfo.ValueMetric => mainResponseButton.selected = true
    case ViewInfo.ErrorMetric => errResponseButton.selected = true
    case ViewInfo.GainMetric => gainResponseButton.selected = true
  }
  
  border = Swing.TitledBorder(border, "Visualization")
}
