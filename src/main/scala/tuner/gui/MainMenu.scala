package tuner.gui

import scala.collection.mutable.Buffer
import scala.collection.mutable.ListBuffer

import scala.swing.Action
import scala.swing.Component
import scala.swing.CutAction
import scala.swing.CopyAction
import scala.swing.KeyStroke._
import scala.swing.Menu
import scala.swing.MenuBar
import scala.swing.MenuItem
import scala.swing.PasteAction
import scala.swing.Separator

import tuner.Tuner
import tuner.project.Project
import tuner.project.Saved

object WindowMenu {
  val menus = new ListBuffer[MainMenu]()

  def updateWindows : Unit = menus.foreach(updateWindows(_))

  def updateWindows(menu:MainMenu) : Unit = {
    
    val windowMenu = menu.windowMenu
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

object MainMenu {
  object ImportSamplesAction {
    def apply(project:tuner.project.Sampler) = {
      Action("Import Samples…") {
        FileChooser.loadFile("Select Samples") foreach {filename =>
          project.importSamples(new java.io.File(filename))
        }
        project match {
          case p:tuner.project.Saved => p.save()
        }
      }
    }
  }
}

/**
 * The main menu for the application
 */
class MainMenu(project:Option[Project]) extends MenuBar {
  
  def this(p:Project) = this(Some(p))
  def this() = this(None)

  val importSamples = new MenuItem("Import Samples…") {
    enabled = false
  }
  /*
  Action("Import Samples…") {
    val fc = new FileChooser {
      title = "Select samples file"
    }
    fc.showOpenDialog(null) match {
      case FileChooser.Result.Approve =>
      case _ =>
    }
  }
  */

  // File menu
  contents += new Menu("File") {
    contents += new MenuItem(new Action("New Project…") {
      accelerator = Some(CommandKey('N'))
      override def apply = Tuner.startNewProject
    })

    contents += new Separator

    contents += new MenuItem(new Action("Open Project…") {
      accelerator = Some(CommandKey('O'))
      //accelerator = Some(javax.swing.KeyStroke.getKeyStroke('O', java.awt.event.InputEvent.META_MASK))
      def apply() = Tuner.openProject
    })
    contents += new MenuItem("Recent Projects")

    contents += new Separator
    
    project match {
      case Some(proj) => 
        contents += new MenuItem(new Action("Save") {
          accelerator = Some(CommandKey('S'))
          override def apply = proj match {
            case p:Saved => p.save()
            case _ => Tuner.saveProjectAs(proj)
          }
        })
        contents += new MenuItem(new Action("Save As…") {
          accelerator = Some(ShiftCommandKey('S'))
          override def apply = Tuner.saveProjectAs(proj)
        })
      case None       =>
        contents += new MenuItem(new Action("Save") {
          accelerator = Some(CommandKey('S'))
          override def apply = {}
        }) {
          enabled = false
        }
        contents += new MenuItem(new Action("Save As…") {
          accelerator = Some(ShiftCommandKey('S'))
          override def apply = {}
        }) {
          enabled = false
        }
    }

    contents += new Separator

    contents += importSamples

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
  WindowMenu.updateWindows(this)

  WindowMenu.menus += this

}

