package tuner.gui

import scala.swing.Button
import scala.swing.BoxPanel
import scala.swing.Orientation
import scala.swing.Publisher
import scala.swing.TextField
import scala.swing.event.ButtonClicked
import scala.swing.event.ValueChanged

/**
 * Element for selecting either a file or directory path
 * 
 * Sends ValueChanged when a new file is selected
 */
class PathPanel extends BoxPanel(Orientation.Horizontal) with Publisher {
  
  val filenameField = new TextField
  val chooseButton = new Button("Browse…")

  var title = ""
  var fileSelector = FileChooser.loadFile _

  contents += filenameField
  contents += chooseButton

  // set up the events system
  listenTo(filenameField)
  listenTo(chooseButton)

  reactions += {
    case ButtonClicked(_) => 
      val ttl = title
      fileSelector(ttl) foreach {filename =>
        filenameField.text = filename
      }
    case ValueChanged(`filenameField`) =>
      publish(new ValueChanged(this))
  }

  def path = filenameField.text
}

