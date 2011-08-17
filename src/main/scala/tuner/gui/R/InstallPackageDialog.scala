package tuner.gui.R

import scala.swing.BorderPanel
import scala.swing.BoxPanel
import scala.swing.Button
import scala.swing.Frame
import scala.swing.Label
import scala.swing.Orientation
import scala.swing.Swing
import scala.swing.event.ButtonClicked

import tuner.Rapp
import tuner.Tuner

class InstallPackageDialog(packages:Seq[String]) extends Frame {

  val message = packages.reduceLeft(_ + ", " + _) + """
    are required and not installed.  Would you like to install them?
  """
  val messagePanel = new Label(message)
  val quitButton = new Button("Quit")
  val installButton = new Button("Install")

  // Various stages of this dialog
  val messageStage = new BorderPanel {
    val buttonPanel = new BoxPanel(Orientation.Horizontal) {
      contents += Swing.HGlue
      contents += quitButton
      contents += Swing.HGlue
    }
    layout(messagePanel) = BorderPanel.Position.Center
    layout(buttonPanel) = BorderPanel.Position.South
  }

  listenTo(quitButton)
  listenTo(installButton)

  reactions += {
    case ButtonClicked(`quitButton`) => 
      Tuner.quit
    case ButtonClicked(`installButton`) => 
      Rapp.installPackage("rJava")
  }

  contents = messageStage
}

