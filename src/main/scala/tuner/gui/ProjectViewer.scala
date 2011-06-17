package tuner.gui

import scala.swing.BorderPanel
import scala.swing.BoxPanel
import scala.swing.ButtonGroup
import scala.swing.CheckBox
import scala.swing.Dialog
import scala.swing.FlowPanel
import scala.swing.Label
import scala.swing.Frame
import scala.swing.Orientation
import scala.swing.RadioButton
import scala.swing.Swing
import scala.swing.TablePanel
import scala.swing.event.ButtonClicked
import scala.swing.event.DialogClosing
import scala.swing.event.ValueChanged

import tuner.Config
import tuner.Project
import tuner.Tuner
import tuner.gui.event.AddSamples
import tuner.gui.event.CandidateChanged
import tuner.gui.event.HistoryAdd
import tuner.gui.event.SliceChanged

class ProjectViewer(project:Project) extends Frame {
  
  title = project.name
  menuBar = MainMenu

  println("here")
  val mainResponseButton = new RadioButton("Value")
  val errResponseButton = new RadioButton("Error")
  val gainResponseButton = new RadioButton("Gain")

  val gradientGlyphButton = new CheckBox("Gradient")
  val regionGlyphButton = new CheckBox("Region") {
    selected = project.viewInfo.showRegion
  }
  val sampleLineGlyphButton = new CheckBox("Line to Sample") {
    selected = project.viewInfo.showSampleLine
  }

  new ButtonGroup(mainResponseButton, errResponseButton, gainResponseButton)

  val mainPlotPanel = new MainPlotPanel(project)

  val controlPanel = new ControlPanel(project)
  // Need this reference for later
  val plotControls = controlPanel.controlsTab

  val paretoPanel = new ParetoPanel(project) {
    border = Swing.TitledBorder(border, "Pareto")
    minimumSize = new java.awt.Dimension(Config.paretoDims._1, 
                                         Config.paretoDims._2)
    maximumSize = preferredSize
  }

  contents = new TablePanel(List(305,TablePanel.Size.Fill), 
                            List(TablePanel.Size.Fill)) {
  
    val responseControlPanel = new BoxPanel(Orientation.Horizontal) {
      contents += Swing.HGlue
      contents += mainResponseButton
      contents += Swing.HGlue
      contents += errResponseButton
      contents += Swing.HGlue
      contents += gainResponseButton
      contents += Swing.HGlue
  
      border = Swing.TitledBorder(border, "View")
    }

    val glyphControlPanel = new BoxPanel(Orientation.Horizontal) {
      contents += Swing.HGlue
      contents += gradientGlyphButton
      contents += Swing.HGlue
      contents += regionGlyphButton
      contents += Swing.HGlue
      contents += sampleLineGlyphButton
      contents += Swing.HGlue

      border = Swing.TitledBorder(border, "Glyphs")
    }
  
    val histogramPanel = new ResponseStatsPanel(project) {
      border = Swing.TitledBorder(border, "Response Histograms")
    }
  
    val leftPanel = new BoxPanel(Orientation.Vertical) {
      contents += paretoPanel
      contents += responseControlPanel
      contents += glyphControlPanel
      contents += histogramPanel
    }

    val rightPanel = new BoxPanel(Orientation.Vertical) {
      contents += mainPlotPanel
      contents += controlPanel
    }

    layout(leftPanel) = (0,0)
    layout(rightPanel) = (1,0)
  }

  listenTo(mainPlotPanel)
  listenTo(mainResponseButton)
  listenTo(errResponseButton)
  listenTo(gainResponseButton)
  listenTo(regionGlyphButton)
  listenTo(sampleLineGlyphButton)
  listenTo(paretoPanel)
  listenTo(controlPanel.historyTab)
  listenTo(controlPanel.candidatesTab)
  listenTo(controlPanel.localTab)
  listenTo(plotControls)

  reactions += {
    case CandidateChanged(_, newCand) =>
      project.updateCandidates(newCand)
      controlPanel.candidatesTab.updateTable
    case SliceChanged(_, sliceInfo) => 
      sliceInfo.foreach {case (fld, v) =>
        controlPanel.controlsTab.sliceSliders.get(fld).foreach {slider =>
          slider.value = v
        }
      }
    case AddSamples(_) => 
      openSamplerDialog
    case HistoryAdd(_, sliceInfo) =>
      project.history.add(sliceInfo)
      controlPanel.historyTab.updateTable
    case ButtonClicked(`mainResponseButton`) =>
      project.viewInfo.currentMetric = Project.ValueMetric
    case ButtonClicked(`errResponseButton`) =>
      project.viewInfo.currentMetric = Project.ErrorMetric
    case ButtonClicked(`gainResponseButton`) =>
      project.viewInfo.currentMetric = Project.GainMetric
    case ButtonClicked(`sampleLineGlyphButton`) =>
      project.viewInfo.showSampleLine = sampleLineGlyphButton.selected
    case ButtonClicked(`regionGlyphButton`) =>
      project.viewInfo.showRegion = regionGlyphButton.selected
  }

  // Update which metric we're looking at
  project.viewInfo.currentMetric match {
    case Project.ValueMetric => mainResponseButton.selected = true
    case Project.ErrorMetric => errResponseButton.selected = true
    case Project.GainMetric => gainResponseButton.selected = true
  }

  override def open = {
    super.open

    // See if there are new responses we need to deal with
    if(project.newFields.length > 0) {
      println("new fields detected")
      val rs = new ResponseSelector(project, this)
      listenTo(rs)
      reactions += {
        case DialogClosing(`rs`, result) => result match {
          case Dialog.Result.Ok => 
            rs.selections.foreach {case (fld, sel) => sel match {
              case ResponseSelector.Ignore => 
                project.ignoreFields = fld :: project.ignoreFields
              case ResponseSelector.Minimize => 
                project.responses = (fld, true) :: project.responses
              case ResponseSelector.Maximize => 
                project.responses = (fld, false) :: project.responses
            }}
            project.save(project.savePath)
          case Dialog.Result.Cancel =>
            this.close
        }
      }
      rs.open
    }
  }

  private def openSamplerDialog = {
    val samplerDialog = new SamplerDialog(project, this)
    listenTo(samplerDialog)
    reactions += {
      case DialogClosing(`samplerDialog`, result) => result match {
        case Dialog.Result.Ok => 
          project.save(project.savePath)
          Tuner.reloadProject(project)
        case Dialog.Result.Cancel =>
          project.newSamples.clear
      }
    }
    samplerDialog.open
  }

}

