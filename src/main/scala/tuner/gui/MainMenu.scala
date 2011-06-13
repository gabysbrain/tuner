package tuner.gui

import scala.swing.Menu
import scala.swing.MenuBar
import scala.swing.MenuItem
import scala.swing.Separator

/**
 * The main menu for the application
 */
object MainMenu extends MenuBar {
  
  // File menu
  contents += new Menu("File") {
    contents += new MenuItem("New Project…")

    contents += new Separator

    contents += new MenuItem("Project Chooser…")
    contents += new MenuItem("Open Project…")
    contents += new MenuItem("Recent Projects")

    contents += new Separator
    
    contents += new MenuItem("Save")
    contents += new MenuItem("Save As…")

    contents += new Separator

    contents += new MenuItem("Quit")
  }

  // Edit menu
  contents += new Menu("Edit") {
    contents += new MenuItem("Cut")
    contents += new MenuItem("Copy")
    contents += new MenuItem("Paste")
  }

  // Window menu
  contents += new Menu("Window") {
    contents += new MenuItem("Project Chooser")

    contents += new Separator

    contents += new MenuItem("Info Window")
    contents += new MenuItem("Local Region Analyzer")
    contents += new MenuItem("Control Window")
    contents += new MenuItem("Candidate Points")
    contents += new MenuItem("History Browser")

    contents += new Separator

  }
}

