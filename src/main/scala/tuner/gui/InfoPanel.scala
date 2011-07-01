package tuner.gui

import scala.swing.BorderPanel
import scala.swing.BoxPanel
import scala.swing.Orientation
import scala.swing.Label
import scala.swing.Panel
import scala.swing.ScrollPane
import scala.swing.Swing
import scala.swing.Table

import tuner.Config
import tuner.project.Viewable

import java.awt.Dimension
import javax.swing.table.AbstractTableModel

class InfoPanel(project:Viewable) extends BoxPanel(Orientation.Vertical) {

  val infoTable = {
    val columnNames = project.inputFields ++ 
                      project.responseFields.flatMap({rf =>
                        List(rf, rf + " Error", rf + " Gain")
                      })
    val initialData:Array[Array[Any]] = Array(
      "Estimate" +: Array.fill(columnNames.length)(""),
      "Nearest Sample" +: Array.fill(columnNames.length)(""))
    new Table(initialData, " " :: columnNames) {
      autoResizeMode = Table.AutoResizeMode.Off
      //minimumSize = new Dimension(Int.MaxValue, 70)
    }
  }
  infoTable.peer.getColumnModel.getColumn(0).setPreferredWidth(100)

  val sampleImagePanel = new PImagePanel(Config.sampleImageSize, 
                                         Config.sampleImageSize)

  val imagePanel = project.previewImages.map {pi =>
    new BoxPanel(Orientation.Horizontal) {
      maximumSize = new Dimension(Int.MaxValue, 150)
      preferredSize = new Dimension(Int.MaxValue, 150)
    
      contents += Swing.HGlue
      contents += new BoxPanel(Orientation.Vertical) {
        /*
        layout(sampleImagePanel) = BorderPanel.Position.Center
        layout(new Label("Closest Sample")) = BorderPanel.Position.South
        */
        contents += Swing.VGlue
        contents += sampleImagePanel
        contents += new Label("Closest Sample")
        contents += Swing.VGlue
      }
      contents += Swing.HGlue
    }
  }

  contents += new ScrollPane {
    contents = infoTable
    //horizontalScrollBarPolicy = ScrollPane.BarPolicy.Always
    maximumSize = new Dimension(Int.MaxValue, 70)
    preferredSize = new Dimension(Int.MaxValue, 70)
  }
  contents += Swing.VGlue

  imagePanel.foreach {ip =>
    contents += ip
    contents += Swing.VGlue
  }

  // Set up the initial table
  updateView

  def updateView = {
    val slice = project.viewInfo.currentSlice
    val closestSample = project.closestSample(slice.toList).toMap
    val estimates = project.estimatePoint(slice.toList)

    // Update the independent dimensions first
    project.inputFields.zipWithIndex.foreach {case (fld, i) =>
      infoTable.update(0, i+1, slice.getOrElse(fld, ""))
      infoTable.update(1, i+1, closestSample.getOrElse(fld, ""))
    }

    // Also update the response fields
    project.responseFields.zipWithIndex.foreach {case (fld, i) =>
      val col = 1 + project.inputFields.length + 3 * i
      val (est, err, gain) = estimates.getOrElse(fld, ("","",""))
      infoTable.update(0, col, est)
      infoTable.update(0, col+1, err)
      infoTable.update(0, col+2, gain)
      infoTable.update(1, col, closestSample.getOrElse(fld, ""))
    }

    // Update the closest image
    project.previewImages.foreach {pi =>
      val img = pi.image(sampleImagePanel.applet, 
                         closestSample("rowNum").toInt)
      sampleImagePanel.image = img
    }
  }

}

