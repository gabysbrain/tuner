package tuner.gui

import scala.swing.BoxPanel
import scala.swing.Button
import scala.swing.Label
import scala.swing.Orientation
import scala.swing.Swing
import scala.swing.TablePanel

import tuner.Config
import tuner.Project

class LocalPanel(project:Project) extends BoxPanel(Orientation.Vertical) {
  
  val radiusSliders = project.inputFields.map {fld =>
    val (minVal, maxVal) = project.currentZoom.range(fld)
    val maxRadius = (minVal + maxVal) / 2
    val slider = new SpinSlider(0f, maxRadius, Config.sliderResolution)
    slider.value = 0f
    (fld, slider)
  } toMap

  val shapeSelector = new RegionShapeCombo
  val sampleButton = new Button("Add Samples")

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
}

