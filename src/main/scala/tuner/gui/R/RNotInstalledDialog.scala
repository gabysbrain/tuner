package tuner.gui.R

import scala.swing.BorderPanel
import scala.swing.BoxPanel
import scala.swing.Button
import scala.swing.Frame
import scala.swing.Label
import scala.swing.Orientation
import scala.swing.Swing
import scala.swing.event.ButtonClicked

import tuner.Tuner

object RNotInstalledDialog extends Frame {

  val message = """
    <html>
    R is not installed.  Please install R before using Tuner.<br/>

    R can be found at <a href="http://cran.r-project.org">CRAN</a>
    </html>
  """
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

