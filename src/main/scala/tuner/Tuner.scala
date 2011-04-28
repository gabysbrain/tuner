package tuner

import scala.swing._
import scala.swing.event._

object Tuner extends SimpleSwingApplication {

  override def main(args:Array[String]) = {
    System.setProperty("apple.laf.useScreenMenuBar", "true")
    System.setProperty("apple.awt.showGrowBox", "true")
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Tuner")
    super.main(args)
  }

  def top = new MainFrame {
    title = "Swing App"

    menuBar = new MenuBar {
      contents += new Menu("A Menu") {
        contents += new MenuItem("An item")
        contents += new MenuItem(Action("Action item") { println(title) })
        contents += new Separator
        contents += new CheckMenuItem("Check me")
      }
      contents += new Menu("Empty Menu")
    }
    
    val button = new Button {
      text = "Click Me"
    }
    val label = new Label {
      text = "No button clicks"
    }

    contents = new BoxPanel(Orientation.Vertical) {
      contents += button
      contents += label
      border = Swing.EmptyBorder(30, 30, 10, 30)
    }

    listenTo(button)
    var clicks = 0
    reactions += {
      case ButtonClicked(b) =>
        clicks += 1
        label.text = "Num clicks " + clicks
    }
  }
  
}

