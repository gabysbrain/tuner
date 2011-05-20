package tuner.gui

import tuner.Config

class ParetoPanel 
    extends P5Panel(Config.paretoDims._1, Config.paretoDims._2, P5Panel.OpenGL) {

  def draw = {
    applet.background(Config.backgroundColor)
  }

}

