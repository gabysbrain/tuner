package tuner.gui

import scala.swing.BoxPanel
import scala.swing.ComboBox
import scala.swing.Label
import scala.swing.Orientation
import scala.swing.FloatRangeSlider
import scala.swing.ScrollPane
import scala.swing.Swing
import scala.swing.TablePanel
import scala.swing.event.SelectionChanged
import scala.swing.event.ValueChanged

import tuner.Config
import tuner.project.Viewable

/**
 * Contains the controls that let the user select the focus point,
 * zoom level, and which response values to show.
 */
class PlotControlsPanel(project:Viewable) 
    extends BoxPanel(Orientation.Vertical) {
  
  // Create dim sliders for each input dimension
  val sliceSliders = project.inputFields.map {fld =>
    val (minVal, maxVal) = project.inputs.range(fld)
    val slider = new SpinSlider(minVal, maxVal, Config.sliderResolution)
    slider.value = project.viewInfo.currentSlice(fld)
    listenTo(slider)
    reactions += {
      case ValueChanged(`slider`) =>
        if(slider.value != project.viewInfo.currentSlice(fld)) {
          project.viewInfo.updateSlice(fld, slider.value)
          publish(new ValueChanged(PlotControlsPanel.this))
        }
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
  val resp1Combo = new ComboBox("None" :: project.responseFields)
  val resp2Combo = new ComboBox("None" :: project.responseFields)

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
      }
      publish(new ValueChanged(PlotControlsPanel.this))
    case SelectionChanged(`resp2Combo`) => resp2Combo.selection.item match {
        case "None" => project.viewInfo.response2View = None
        case x      => project.viewInfo.response2View = Some(x)
      }
      publish(new ValueChanged(PlotControlsPanel.this))
  }

  val (slicePanel, zoomPanel) = {
    val cellWidths:List[Double] = List(0.25, TablePanel.Size.Fill)
    val hgts:List[Double] = List.fill(sliceSliders.size)(25)
    val cellHeights:List[Double] = 
      hgts :+ TablePanel.Size.Fill
  
    val slices = new TablePanel(cellWidths, cellHeights) {
      project.inputFields.sorted.zipWithIndex.foreach {case (fld,i) =>
        layout(new Label(fld)) = (0, i, TablePanel.HorizAlign.Right)
        layout(sliceSliders(fld)) = (1, i, TablePanel.HorizAlign.Full)
      }

      border = Swing.TitledBorder(border, "Slice")
    }

    val zooms = new TablePanel(cellWidths, cellHeights) {
      project.inputFields.sorted.zipWithIndex.foreach {case (fld, i) =>
        layout(new Label(fld)) = (0, i, TablePanel.HorizAlign.Right)
        layout(zoomSliders(fld)) = (1, i, TablePanel.HorizAlign.Full)
      }

      border = Swing.TitledBorder(border, "Zoom")
    }
    (slices, zooms)
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
    //minimumSize = preferredSize
  }

  val myWidth = Config.mainPlotDims._1 - 30
  contents += new ScrollPane {
    //contents = new TablePanel(2, 1) {
    contents = new TablePanel(List(myWidth/2.0, myWidth/2.0), 
                              List(TablePanel.Size.Fill)) {
      layout(slicePanel) = (0, 0)
      layout(zoomPanel) = (1, 0)
    }
    //horizontalScrollBarPolicy = ScrollPane.BarPolicy.Never
  }

  contents += responsePanel
    
}

