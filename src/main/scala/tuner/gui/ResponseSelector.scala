package tuner.gui

import scala.swing.BoxPanel
import scala.swing.Button
import scala.swing.ButtonGroup
import scala.swing.Dialog
import scala.swing.Label
import scala.swing.Orientation
import scala.swing.RadioButton
import scala.swing.Swing
import scala.swing.TextField
import scala.swing.event.ButtonClicked
import tuner.gui.event.DialogClosing

import tuner.Tuner
import tuner.project.NewResponses

object ResponseSelector {
  abstract class Response 
  case object Ignore extends Response
  case object Minimize extends Response
  case object Maximize extends Response
}

class ResponseSelector(project:NewResponses) extends Window(project) {
  title = "Select Responses"

  val okButton = new Button("Ok")
  val cancelButton = new Button("Cancel")

  /*
  val responseTable = new ControlTable(List("Name", "Maximize")) {
    def controlRow = List(
      new TextField,
      new RadioButton("Min"),
      new RadioButton("Max")
    )
  }
  */

  val radioMap = project.newFields.sorted.map {fld =>
    val ignoreRadio = new RadioButton("Ignore")
    val minRadio = new RadioButton("Min")
    val maxRadio = new RadioButton("Max")
    val buttonGroup = new ButtonGroup(ignoreRadio, minRadio, maxRadio)
    buttonGroup.select(ignoreRadio)
    (fld -> (ignoreRadio, minRadio, maxRadio))
  }

  val responseTable = new TablePanel(4, project.newFields.length) {

    radioMap.zipWithIndex.foreach {case ((fld, (ig,min,max)), i) =>
      layout(new Label(fld)) = (0, i)
      layout(ig) = (1, i)
      layout(min) = (2, i)
      layout(max) = (3, i)
    }
  }

  listenTo(okButton)
  listenTo(cancelButton)

  contents = new BoxPanel(Orientation.Vertical) {
    val buttonPanel = new BoxPanel(Orientation.Horizontal) {
      contents += Swing.HGlue
      contents += okButton
      contents += cancelButton
    }

    contents += responseTable
    contents += buttonPanel
  }

  reactions += {
    case ButtonClicked(`okButton`) =>
      selections.foreach {case (fld,resp) => resp match {
        case ResponseSelector.Minimize => project.addResponse(fld, true)
        case ResponseSelector.Maximize => project.addResponse(fld, false)
        case ResponseSelector.Ignore   => project.addIgnore(fld)
      }}
      project.save
      dispose
    case ButtonClicked(`cancelButton`) =>
      dispose
  }

  def selections : List[(String,ResponseSelector.Response)] = {
    radioMap.map {case (fld, (ig, min, max)) =>
      if(min.selected) {
        (fld, ResponseSelector.Minimize)
      } else if(max.selected) {
        (fld, ResponseSelector.Maximize)
      } else {
        (fld, ResponseSelector.Ignore)
      }
    } toList
  }

}

