package tuner.gui

import scala.swing.BoxPanel
import scala.swing.Orientation
import scala.swing.Swing
import scala.swing.TabbedPane
import scala.swing.event.SelectionChanged
import scala.swing.event.ValueChanged

import tuner.gui.event.NewDesignSelected
import tuner.gui.event.NewResponseSelected

import tuner.Config
import tuner.Sampler

/**
 * The the panel for selecting how many sample points to add, either
 * internally by Tuner or externally loaded
 */
class SamplerPanel(project:tuner.project.Sampler,
                   newSamples:((Int,Sampler.Method) => Unit)) 
    extends BoxPanel(Orientation.Vertical) {
  
  // Panel for controling new samples from scratch
  val sampleGenPanel = new SampleGenerationPanel(newSamples)
  // Panel for importing already run samples
  val sampleImportPanel = new SampleImportPanel(newSamples)
  // SPloM view of samples
  val splomPanel = new SamplerSplomPanel(project)

  // Tabbed pane controlling which sampling method
  val sampleControlsTabs = new TabbedPane {
    border = Swing.TitledBorder(border, "Sampling")

    pages += new TabbedPane.Page("Generate", sampleGenPanel)
    pages += new TabbedPane.Page("Import", sampleImportPanel)
  }

  // Initialize the response selector combo box
  sampleImportPanel.responses = projectResponseFields

  // Add everything we need
  contents += sampleControlsTabs
  contents += splomPanel

  // Set up the events
  listenTo(sampleGenPanel)
  listenTo(sampleImportPanel)
  listenTo(sampleControlsTabs.selection)

  reactions += {
    case ValueChanged(`sampleGenPanel`) =>
      splomPanel.redraw
      publish(new ValueChanged(SamplerPanel.this))
    case NewDesignSelected(`sampleImportPanel`) =>
      project.importSamples(sampleImportPanel.designFile)
      sampleImportPanel.responses = projectResponseFields
      splomPanel.selectedResponse = sampleImportPanel.selectedResponse
      splomPanel.redraw
    case NewResponseSelected(`sampleImportPanel`, response) =>
      splomPanel.selectedResponse = Some(response)
      splomPanel.redraw
    case ValueChanged(`sampleImportPanel`) =>
      splomPanel.redraw
      publish(new ValueChanged(SamplerPanel.this))
    case SelectionChanged(`sampleControlsTabs`) =>
      sampleControlsTabs.selection.page.title match {
        case "Generate" => 
          splomPanel.drawSamples = project.newSamples
          splomPanel.selectedResponse = None
        case "Import"   => 
          splomPanel.drawSamples = project.designSites
          splomPanel.selectedResponse = sampleImportPanel.selectedResponse
        case _          => // do nothing
      }
      splomPanel.redraw
      publish(new SelectionChanged(SamplerPanel.this))
  }

  def numSamples : Int = sampleGenPanel.numSamples

  def saveSamples = {
    FileChooser.saveFile("Save Samples") foreach {filename =>
      project.newSamples.toCsv(filename)
    }
  }

  /**
   * Fields that are not input fields in the project
   */
  def projectResponseFields = 
    project.designSites.fieldNames.diff(project.sampleRanges.dimNames)
      .filterNot(_ == "rowNum")
}

