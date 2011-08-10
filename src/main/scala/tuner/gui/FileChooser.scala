package tuner.gui

import java.awt.FileDialog

object FileChooser {

  def saveFile(title:String = "Save") : Option[String] = {
    val fc = new FileDialog(null:java.awt.Dialog, title, FileDialog.SAVE)
    fc.setVisible(true)
    val fn = fc.getFile
    if(fn == null) None
    else           println(fn); Some(fn)
  }

  def loadFile(title:String = "Load") : Option[String] = {
    val fc = new FileDialog(null:java.awt.Dialog, title, FileDialog.LOAD)
    fc.setVisible(true)
    val fn = fc.getFile
    if(fn == null) None
    else           Some(fn)
  }

  def loadDirectory(title:String = "Load") : Option[String] = {
    val fc = new FileDialog(null:java.awt.Dialog, title, FileDialog.LOAD)
    fc.setFilenameFilter(new java.io.FilenameFilter {
      def accept(dir:java.io.File, name:String) : Boolean = {
        new java.io.File(dir, name).isDirectory
      }
    })
    fc.setVisible(true)
    val fn = fc.getFile
    if(fn == null) None
    else           Some(fn)
  }

}

