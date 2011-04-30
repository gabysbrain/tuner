package tuner.gui

import scala.swing.MainFrame
import scala.swing.BorderPanel

/**
 * Project window that allows the user to select which project they want to
 * examine.
 */
object ProjectWindow extends MainFrame {

  title = "Select Project"

  menuBar = MainMenu

  contents = new BorderPanel {
  }

}

