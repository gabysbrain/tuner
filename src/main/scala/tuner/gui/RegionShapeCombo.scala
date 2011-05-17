package tuner.gui

import scala.swing.ComboBox

import tuner.Region

class RegionShapeCombo extends ComboBox(List("Box", "Ellipse")) {

  def value : Region.Shape = selection.item match {
    case "Box" => Region.Box
    case "Ellipse" => Region.Ellipse
  }
}

