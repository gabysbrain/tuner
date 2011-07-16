package tuner.gui

import scala.swing.Action
import scala.swing.Menu
import scala.swing.MenuBar
import scala.swing.MenuItem
import scala.swing.Separator

import tuner.Tuner

/**
 * The main menu for the application
 */
object MainMenu extends MenuBar {
  
  // File menu
  contents += new Menu("File") {
    contents += new MenuItem("New Project…")

    contents += new Separator

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
  val windowMenu = new Menu("Window")
  contents += windowMenu
  updateWindows

  def updateWindows = {
    
    windowMenu.contents.clear

    windowMenu.contents += new MenuItem(Action("Project Chooser…") {
      ProjectChooser.open
    })

    windowMenu.contents += new Separator

    windowMenu.contents += new MenuItem("Info Window")
    windowMenu.contents += new MenuItem("Local Region Analyzer")
    windowMenu.contents += new MenuItem("Control Window")
    windowMenu.contents += new MenuItem("Candidate Points")
    windowMenu.contents += new MenuItem("History Browser")

    windowMenu.contents += new Separator

    // Add all the open windows
    Tuner.openWindows.foreach {window => 
      windowMenu.contents += new MenuItem(window.project.name)
    }
  }
}

