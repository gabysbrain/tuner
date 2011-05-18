package tuner.gui

import scala.swing.ComboBox

import tuner.Region
import tuner.BoxRegion
import tuner.EllipseRegion

class RegionShapeCombo extends ComboBox(List("Box", "Ellipse")) {

  def value : Region.Shape = selection.item match {
    case "Box" => Region.Box
    case "Ellipse" => Region.Ellipse
  }
  def value_=(s:Region) = s match {
    case _:BoxRegion => selection.item = "Box"
    case _:EllipseRegion => selection.item = "Ellipse"
  }
}

