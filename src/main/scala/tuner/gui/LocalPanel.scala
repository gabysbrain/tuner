package tuner.gui

import scala.swing.BoxPanel
import scala.swing.Button
import scala.swing.Label
import scala.swing.Orientation
import scala.swing.ScrollPane
import scala.swing.Swing
import scala.swing.TablePanel
import scala.swing.event.ButtonClicked
import scala.swing.event.DialogClosing
import scala.swing.event.SelectionChanged
import scala.swing.event.ValueChanged

import tuner.Config
import tuner.Project
import tuner.Region

import java.awt.Dimension

class LocalPanel(project:Project) extends BoxPanel(Orientation.Vertical) {
  
  val statsTable = new RegionStatsTable(project)

  val radiusSliders = project.inputFields.map {fld =>
    val (minVal, maxVal) = project.viewInfo.currentZoom.range(fld)
    val maxRadius = (minVal + maxVal) / 2
    val slider = new SpinSlider(0f, maxRadius, Config.sliderResolution)
    slider.value = project.region.radius(fld)
    listenTo(slider)
    reactions += {
      case ValueChanged(`slider`) => 
        project.region.setRadius(fld, slider.value)
        statsTable.updateStats
    }
    (fld, slider)
  } toMap

  val shapeSelector = new RegionShapeCombo {
    value = project.region
  }
  val sampleButton = new Button("Add Samples")

  listenTo(shapeSelector.selection)
  listenTo(sampleButton)

  reactions += {
    case SelectionChanged(`shapeSelector`) =>
      val oldRegion = project.region
      project.region = Region(shapeSelector.value, project)
      // If we change the region make sure to update the radius
      project.inputFields.foreach {fld =>
        project.region.setRadius(fld, oldRegion.radius(fld))
      }
      statsTable.updateStats
    case ButtonClicked(`sampleButton`) =>
      openSamplerDialog
  }

  contents += Swing.VGlue
  contents += new BoxPanel(Orientation.Horizontal) {
    contents += Swing.HGlue
    contents += new TablePanel(2, radiusSliders.size) {
      radiusSliders.zipWithIndex.foreach {case ((fld, slider), i) =>
        layout(new Label(fld)) = (0, i)
        layout(slider) = (1, i)
      }

      border = Swing.TitledBorder(border, "Radius")
    }
    contents += Swing.HGlue
    contents += new BoxPanel(Orientation.Vertical) {
      contents += new TablePanel(2, 1) {
        layout(new Label("Shape")) = (0, 0)
        layout(shapeSelector) = (1, 0)
      }
      contents += sampleButton

      border = Swing.TitledBorder(border, "Region")
    }
    contents += Swing.HGlue
  }
  contents += Swing.VGlue
  contents += new ScrollPane {
    contents = statsTable
  }

  private def openSamplerDialog = {
    /*
    val samplerDialog = new SamplerDialog(project, this.parent)
    listenTo(samplerDialog)
    reactions += {
      case DialogClosing(`samplerDialog`, result) =>
        println(result)
    }
    samplerDialog.open
    */
  }
}

