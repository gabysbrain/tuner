package tuner.gui

import scala.collection.immutable.SortedMap
import scala.swing.BoxPanel
import scala.swing.Button
import scala.swing.Dialog
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
import tuner.Region
import tuner.gui.event.AddSamples
import tuner.project.Viewable

import java.awt.Dimension

class LocalPanel(project:Viewable) extends BoxPanel(Orientation.Vertical) {
  
  val statsTable = new RegionStatsTable(project) {
    //minimumSize = new Dimension(620, 100)
    //preferredSize = new Dimension(620, 100)
  }

  val radiusSliders:SortedMap[String,SpinSlider] = 
    SortedMap[String,SpinSlider]() ++
    project.inputFields.map {fld =>
      val (minVal, maxVal) = project.viewInfo.currentZoom.range(fld)
      val maxRadius = (minVal + maxVal) / 2
      val slider = new SpinSlider(0f, maxRadius, Config.sliderResolution)
      slider.value = project.region.radius(fld)
      listenTo(slider)
      reactions += {
        case ValueChanged(`slider`) => 
          project.region.setRadius(fld, slider.value)
          statsTable.updateStats
          publish(new ValueChanged(LocalPanel.this))
      }
      (fld, slider)
    }

  val shapeSelector = new RegionShapeCombo {
    value = project.region
  }
  val sampleButton = new Button("Add Samples")

  listenTo(shapeSelector.selection)
  listenTo(sampleButton)
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
      publish(new AddSamples(this))
  }

  //contents += Swing.VGlue
  contents += new BoxPanel(Orientation.Horizontal) {
    contents += {
      val cellWidths:List[Double] = List(0.25, TablePanel.Size.Fill)
      val hgts:List[Double] = List.fill(radiusSliders.size)(25)
      val cellHeights:List[Double] = 
        hgts :+ TablePanel.Size.Fill

      new ScrollPane {
        contents = new TablePanel(cellWidths, cellHeights) {
          radiusSliders.zipWithIndex.foreach {case ((fld, slider), i) =>
            layout(new Label(fld)) = (0, i)
            layout(slider) = (1, i)
          }
        }
        
        border = Swing.TitledBorder(border, "Radius")
      }
    }
    contents += new BoxPanel(Orientation.Vertical) {
      contents += Swing.VGlue
      contents += sampleButton
      contents += Swing.VGlue

      border = Swing.TitledBorder(border, "Sampling")
    }
  }
  //contents += Swing.VGlue
  contents += new ScrollPane {
    contents = statsTable
  }

}

