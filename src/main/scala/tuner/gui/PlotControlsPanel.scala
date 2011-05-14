package tuner.gui

import scala.swing.BoxPanel
import scala.swing.ComboBox
import scala.swing.Label
import scala.swing.Orientation
import scala.swing.FloatRangeSlider
import scala.swing.Swing
import scala.swing.TablePanel

import tuner.Config
import tuner.Project

class PlotControlsPanel(project:Project) 
    extends BoxPanel(Orientation.Vertical) {
  
  // Create dim sliders for each input dimension
  val sliceSliders = project.inputFields.map {fld =>
    val (minVal, maxVal) = project.inputs.range(fld)
    val slider = new SpinSlider(minVal, maxVal, Config.sliderResolution)
    (fld, slider)
  } toMap

  // Create zoom sliders for each input dimension
  val zoomSliders = project.inputFields.map {fld =>
    val (minVal, maxVal) = project.inputs.range(fld)
    val slider = new FloatRangeSlider(minVal, maxVal, Config.sliderResolution)
    (fld, slider)
  } toMap

  // Create combo boxes for the 2 possible outputs
  val resp1Combo = new ComboBox("None" :: project.responses.map {_._1})
  val resp2Combo = new ComboBox("None" :: project.responses.map {_._1})

  val slicePanel = new TablePanel(2, sliceSliders.size) {
    project.inputFields.sorted.zipWithIndex.foreach {case (fld,i) =>
      layout(new Label(fld)) = (0, i)
      layout(sliceSliders(fld)) = (1, i)
    }

    border = Swing.TitledBorder(border, "Slice")
  }

  val zoomPanel = new TablePanel(2, zoomSliders.size) {
    project.inputFields.sorted.zipWithIndex.foreach {case (fld, i) =>
      layout(new Label(fld)) = (0, i)
      layout(zoomSliders(fld)) = (1, i)
    }
  }

  val responsePanel = new BoxPanel(Orientation.Horizontal) {
    contents += Swing.HGlue
    contents += new Label("Response 1")
    contents += resp1Combo
    contents += Swing.HGlue
    contents += new Label("Response 2")
    contents += resp2Combo
    contents += Swing.HGlue
  }

  contents += new BoxPanel(Orientation.Horizontal) {
    Swing.HGlue
    contents += slicePanel
    Swing.HGlue
    contents += zoomPanel
    Swing.HGlue
  }
  contents += responsePanel
    
}

