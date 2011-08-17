package tuner.gui.R

import scala.swing.BorderPanel
import scala.swing.BoxPanel
import scala.swing.Button
import scala.swing.Frame
import scala.swing.Label
import scala.swing.Orientation
import scala.swing.ProgressBar
import scala.swing.Swing
import scala.swing.event.ButtonClicked

import tuner.Rapp
import tuner.Tuner

abstract class InstallPackageDialog(packages:Seq[String]) extends Frame {

  val installPackage:String=>Unit

  val installMessage = packages.reduceLeft(_ + ", " + _) + """
    are required and not installed.  Would you like to install them?
  """
  val installMessagePanel = new Label(installMessage)
  val installStatusPanel = new Label("")
  val quitButton = new Button("Quit")
  val installButton = new Button("Install")
  val cancelButton = new Button("Cancel")

  // Various stages of this dialog
  val messageStage = new BorderPanel {
    val buttonPanel = new BoxPanel(Orientation.Horizontal) {
      contents += Swing.HGlue
      contents += quitButton
      contents += Swing.HGlue
    }
    layout(installMessagePanel) = BorderPanel.Position.Center
    layout(buttonPanel) = BorderPanel.Position.South
  }

  val installStage = new BorderPanel {
    val buttonPanel = new BoxPanel(Orientation.Horizontal) {
      contents += Swing.HGlue
      contents += cancelButton
      contents += Swing.HGlue
    }
    val statusPanel = new BoxPanel(Orientation.Vertical) {
      contents += new ProgressBar {
        indeterminate = true
      }
      contents += installStatusPanel
    }

    layout(statusPanel) = BorderPanel.Position.Center
    layout(statusPanel) = BorderPanel.Position.South
  }

  listenTo(quitButton)
  listenTo(installButton)
  listenTo(cancelButton)

  reactions += {
    case ButtonClicked(`quitButton`) => 
      Tuner.quit
    case ButtonClicked(`cancelButton`) => 
      Tuner.quit
    case ButtonClicked(`installButton`) => 
      contents = installStage
      packages.foreach {pkg =>
        installStatusPanel.text = "Installing " + pkg + "..."
        installPackage(pkg)
      }
  }

  contents = messageStage
}

