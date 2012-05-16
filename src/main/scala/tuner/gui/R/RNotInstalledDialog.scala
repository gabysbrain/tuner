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

object RNotInstalledDialog extends Frame {

  val message = """<html>
    R is not installed %s <br/>
    <br/>
    Please install R or set RHOME before using Tuner.
    R can be found at <a href="http://cran.r-project.org">CRAN</a><br/>
    </html>""".format(Rapp.rPath match {
      case Some(path) => "or R is not in '%s'".format(path)
      case None       => None
    })
  val messagePanel = new Label(message)
  val quitButton = new Button("Quit")

  listenTo(quitButton)

  reactions += {
    case ButtonClicked(`quitButton`) => Tuner.quit
  }

  defaultButton = quitButton

  contents = new BorderPanel {
    val buttonPanel = new BoxPanel(Orientation.Horizontal) {
      contents += Swing.HGlue
      contents += quitButton
      contents += Swing.HGlue
    }
    layout(messagePanel) = BorderPanel.Position.Center
    layout(buttonPanel) = BorderPanel.Position.South
  }
}

