package tuner.gui

import java.awt.FileDialog

object FileChooser {

  // Making this once preserves state between calls
  lazy val dialog = new scala.swing.FileChooser

  def saveFile(title:String = "Save") : Option[String] = {
    System.setProperty("apple.awt.fileDialogForDirectories", "false")
    dialog.title = title
    dialog.fileSelectionMode = scala.swing.FileChooser.SelectionMode.FilesOnly
    dialog.showSaveDialog(null) match {
      case scala.swing.FileChooser.Result.Approve => Some(dialog.selectedFile.getAbsolutePath)
      case _ => None
    }
  }

  def loadFile(title:String = "Load") : Option[String] = {
    System.setProperty("apple.awt.fileDialogForDirectories", "false")
    dialog.title = title
    dialog.fileSelectionMode = scala.swing.FileChooser.SelectionMode.FilesOnly
    dialog.showOpenDialog(null) match {
      case scala.swing.FileChooser.Result.Approve => Some(dialog.selectedFile.getAbsolutePath)
      case _ => None
    }
  }

  def loadDirectory(title:String = "Load") : Option[String] = {
    System.setProperty("apple.awt.fileDialogForDirectories", "true")
    dialog.title = title
    dialog.fileSelectionMode = scala.swing.FileChooser.SelectionMode.DirectoriesOnly
    dialog.showOpenDialog(null) match {
      case scala.swing.FileChooser.Result.Approve => Some(dialog.selectedFile.getAbsolutePath)
      case _ => None
    }
  }

}

