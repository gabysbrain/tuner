package tuner.gui

import scala.swing.Button
import scala.swing.BoxPanel
import scala.swing.FileChooser
import scala.swing.Orientation
import scala.swing.Publisher
import scala.swing.TextField
import scala.swing.event.ButtonClicked
import scala.swing.event.ValueChanged

class PathPanel extends BoxPanel(Orientation.Horizontal) with Publisher {
  
  val filenameField = new TextField
  val chooseButton = new Button("Browse…")

  var title = ""
  var fileSelectionMode = FileChooser.SelectionMode.DirectoriesOnly

  contents += filenameField
  contents += chooseButton

  // set up the events system
  listenTo(filenameField)
  listenTo(chooseButton)

  reactions += {
    case ButtonClicked(_) => 
      val sm = fileSelectionMode
      val ttl = title
      val fc = new FileChooser {
        title = ttl
        fileSelectionMode = sm
      }
      fc.showOpenDialog(filenameField) match {
        case FileChooser.Result.Approve => 
          filenameField.text = fc.selectedFile.getAbsolutePath
      }
    case ValueChanged(`filenameField`) =>
      publish(new ValueChanged(this))
  }

  def path = filenameField.text
}

