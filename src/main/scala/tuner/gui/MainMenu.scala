package tuner.gui

import scala.swing.Action
import scala.swing.CutAction
import scala.swing.CopyAction
import scala.swing.KeyStroke._
import scala.swing.Menu
import scala.swing.MenuBar
import scala.swing.MenuItem
import scala.swing.PasteAction
import scala.swing.Separator

import tuner.Tuner

/**
 * The main menu for the application
 */
object MainMenu extends MenuBar {
  
  // File menu
  contents += new Menu("File") {
    contents += new MenuItem(new Action("New Project...") {
      accelerator = Some(CommandKey('N'))
      override def apply = Tuner.startNewProject
    })

    contents += new Separator

    contents += new MenuItem(new Action("Open Project…") {
      accelerator = Some(CommandKey('O'))
      def apply() = Tuner.openProject
    })
    contents += new MenuItem("Recent Projects")

    contents += new Separator
    
    contents += new MenuItem(new Action("Save") {
      accelerator = Some(CommandKey('S'))
      override def apply = Tuner.saveCurrent
    })
    contents += new MenuItem(new Action("Save As…") {
      accelerator = Some(ShiftCommandKey('S'))
      override def apply = Tuner.saveCurrentAs
    })

    contents += new Separator

    contents += new MenuItem(new Action("Quit") {
      accelerator = Some(CommandKey('Q'))
      override def apply = Tuner.quit
    })
  }

  // Edit menu
  contents += new Menu("Edit") {
    contents += new MenuItem(CutAction("Cut"))
    contents += new MenuItem(CopyAction("Copy"))
    contents += new MenuItem(PasteAction("Paste"))
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

    /*
    windowMenu.contents += new Separator

    windowMenu.contents += new MenuItem("Info Window")
    windowMenu.contents += new MenuItem("Local Region Analyzer")
    windowMenu.contents += new MenuItem("Control Window")
    windowMenu.contents += new MenuItem("Candidate Points")
    windowMenu.contents += new MenuItem("History Browser")
    */

    windowMenu.contents += new Separator

    // Add all the open windows
    Tuner.openWindows.foreach {window => 
      windowMenu.contents += new MenuItem(Action(window.project.name) {
        window.toFront
      })
    }
  }
}

