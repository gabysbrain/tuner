package tuner.gui

import tuner.Config
import tuner.Project

class SamplerSplomPanel(project:Project)
  extends P5Panel(Config.samplerSplomDims._1, 
                  Config.samplerSplomDims._2, 
                  P5Panel.OpenGL) {
  
  //val 
  
  def draw = {
    applet.background(Config.backgroundColor)
  }

}

