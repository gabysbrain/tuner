package tuner.gui

import tuner.Config
import tuner.Project
import tuner.geom.Rectangle
import tuner.gui.widgets.Scatterplot

class SamplerSplomPanel(project:Project)
  extends P5Panel(Config.samplerSplomDims._1, 
                  Config.samplerSplomDims._2, 
                  P5Panel.OpenGL) {
  
  var splomBounds = Rectangle((0f,0f), (0f,0f))
  val sploms = project.inputFields.flatMap({fld1 =>
    project.inputFields.flatMap({fld2 =>
      if(fld1 < fld2) {
        Some(((fld1, fld2), new Scatterplot(Config.sampleDotColor)))
      } else {
        None
      }
    })
  }).toMap
  
  def draw = {
    applet.background(Config.backgroundColor)

    // Compute all the sizes of things
    val totalSize = math.min(width, height) - Config.plotSpacing * 2
    splomBounds = Rectangle((Config.plotSpacing, Config.plotSpacing), 
                            totalSize, totalSize)
    //splomSize = splomBounds /
  }

}

