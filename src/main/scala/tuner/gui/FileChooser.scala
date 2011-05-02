package tuner.gui

import scala.swing.Button
import scala.swing.BoxPanel
import scala.swing.Orientation
import scala.swing.Reactor
import scala.swing.TextField
import scala.swing.event.ButtonClicked

class FileChooser extends BoxPanel(Orientation.Horizontal) with Reactor {
  
  val filenameField = new TextField
  val chooseButton = new Button("Browse…")

  contents += filenameField
  contents += chooseButton

  // set up the events system
  listenTo(chooseButton)

  reactions += {
    case ButtonClicked(_) => println("choose file")
  }
}

