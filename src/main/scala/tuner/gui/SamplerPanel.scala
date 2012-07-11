package tuner.gui

import scala.swing.BoxPanel
import scala.swing.Orientation
import scala.swing.Swing
import scala.swing.event.ValueChanged

import tuner.Config
import tuner.Sampler

/**
 * The the panel for selecting how many sample points to add, either
 * internally by Tuner or externally loaded
 */
class SamplerPanel(project:tuner.project.Sampler,
                   newSamples:((Int,Sampler.Method) => Unit)) 
    extends BoxPanel(Orientation.Vertical) {
  
  val sampleGenPanel = new SampleGenerationPanel(newSamples) {
    border = Swing.TitledBorder(border, "Sampling")
  }

  val splomPanel = new SamplerSplomPanel(project)

  contents += sampleGenPanel
  contents += splomPanel

  // Set up the events
  listenTo(sampleGenPanel)

  reactions += {
    case ValueChanged(`sampleGenPanel`) =>
      splomPanel.redraw
      publish(new ValueChanged(this))
  }

  def numSamples : Int = sampleGenPanel.numSamples

  def saveSamples = {
    FileChooser.saveFile("Save Samples") foreach {filename =>
      project.newSamples.toCsv(filename)
    }
  }

}

