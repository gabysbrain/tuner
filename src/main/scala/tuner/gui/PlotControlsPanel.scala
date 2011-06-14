package tuner.gui

import scala.swing.BoxPanel
import scala.swing.ComboBox
import scala.swing.Label
import scala.swing.Orientation
import scala.swing.FloatRangeSlider
import scala.swing.Swing
import scala.swing.TablePanel
import scala.swing.event.SelectionChanged
import scala.swing.event.ValueChanged

import tuner.Config
import tuner.Project

class PlotControlsPanel(project:Project) 
    extends BoxPanel(Orientation.Vertical) {
  
  // Create dim sliders for each input dimension
  val sliceSliders = project.inputFields.map {fld =>
    val (minVal, maxVal) = project.inputs.range(fld)
    val slider = new SpinSlider(minVal, maxVal, Config.sliderResolution)
    slider.value = project.viewInfo.currentSlice(fld)
    listenTo(slider)
    reactions += {
      case ValueChanged(`slider`) =>
        project.viewInfo.updateSlice(fld, slider.value)
        publish(new ValueChanged(PlotControlsPanel.this))
    }
    (fld, slider)
  } toMap

  // Create zoom sliders for each input dimension
  val zoomSliders = project.inputFields.map {fld =>
    val (minVal, maxVal) = project.inputs.range(fld)
    val slider = new SpinRangeSlider(minVal, maxVal, Config.sliderResolution)
    slider.value = project.viewInfo.currentZoom.range(fld)
    listenTo(slider)
    reactions += {
      case ValueChanged(`slider`) =>
        project.viewInfo.updateZoom(fld, slider.lowValue, slider.highValue)
        publish(new ValueChanged(PlotControlsPanel.this))
    }
    (fld, slider)
  } toMap

  // Create combo boxes for the 2 possible outputs
  val resp1Combo = new ComboBox("None" :: project.responses.map {_._1})
  val resp2Combo = new ComboBox("None" :: project.responses.map {_._1})

  project.viewInfo.response1View foreach {r =>
    resp1Combo.selection.item = r
  }
  project.viewInfo.response2View foreach {r =>
    resp2Combo.selection.item = r
  }

  listenTo(resp1Combo.selection)
  listenTo(resp2Combo.selection)

  reactions += {
    case SelectionChanged(`resp1Combo`) => resp1Combo.selection.item match {
      case "None" => project.viewInfo.response1View = None
      case x      => project.viewInfo.response1View = Some(x)
      publish(new ValueChanged(PlotControlsPanel.this))
    }
    case SelectionChanged(`resp2Combo`) => resp2Combo.selection.item match {
      case "None" => project.viewInfo.response2View = None
      case x      => project.viewInfo.response2View = Some(x)
      publish(new ValueChanged(PlotControlsPanel.this))
    }
  }

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

    border = Swing.TitledBorder(border, "Zoom")
  }

  val responsePanel = new BoxPanel(Orientation.Horizontal) {
    contents += Swing.HGlue
    contents += new Label("Response 1")
    contents += resp1Combo
    contents += Swing.HGlue
    contents += new Label("Response 2")
    contents += resp2Combo
    contents += Swing.HGlue

    border = Swing.TitledBorder(border, "Response")
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

