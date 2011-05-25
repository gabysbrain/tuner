package tuner.gui

import scala.swing.BorderPanel
import scala.swing.BoxPanel
import scala.swing.ButtonGroup
import scala.swing.CheckBox
import scala.swing.Dialog
import scala.swing.FlowPanel
import scala.swing.Label
import scala.swing.MainFrame
import scala.swing.Orientation
import scala.swing.RadioButton
import scala.swing.Swing
import scala.swing.TablePanel
import scala.swing.event.ButtonClicked
import scala.swing.event.DialogClosing

import tuner.Project
import tuner.gui.event.HistoryAdd
import tuner.gui.event.SliceChanged

class ProjectViewer(project:Project) extends MainFrame {
  
  title = project.name
  menuBar = MainMenu

  val mainResponseButton = new RadioButton("Value")
  val errResponseButton = new RadioButton("Error")
  val gainResponseButton = new RadioButton("Gain")

  val gradientGlyphButton = new CheckBox("Gradient")
  val regionGlyphButton = new CheckBox("Region")
  val sampleLineGlyphButton = new CheckBox("Line to Sample")

  new ButtonGroup(mainResponseButton, errResponseButton, gainResponseButton)

  val plot = new MainPlotPanel(project)

  val controlPanel = new ControlPanel(project)

  contents = new TablePanel(List(305,TablePanel.Size.Fill), 
                            List(TablePanel.Size.Fill)) {
    val paretoPanel = new ParetoPanel(project) {

      border = Swing.TitledBorder(border, "Pareto")

      maximumSize = preferredSize
    }
  
    val responseControlPanel = new BoxPanel(Orientation.Horizontal) {
      contents += mainResponseButton
      contents += errResponseButton
      contents += gainResponseButton
  
      border = Swing.TitledBorder(border, "View")
    }

    val glyphControlPanel = new BoxPanel(Orientation.Horizontal) {
      contents += gradientGlyphButton
      contents += regionGlyphButton
      contents += sampleLineGlyphButton

      border = Swing.TitledBorder(border, "Glyphs")
    }
  
    val histogramPanel = new ResponseStatsPanel(project) {
      border = Swing.TitledBorder(border, "Response Histograms")
    }
  
    val mainPlotPanel = plot
  
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

  listenTo(plot)
  listenTo(mainResponseButton)
  listenTo(errResponseButton)
  listenTo(gainResponseButton)
  listenTo(regionGlyphButton)

  reactions += {
    case SliceChanged(_, sliceInfo) => 
      sliceInfo.foreach {case (fld, v) =>
        controlPanel.controlsTab.sliceSliders(fld).value = v
      }
    case HistoryAdd(_, sliceInfo) =>
      project.history.add(sliceInfo)
      controlPanel.historyTab.updateTable
    case ButtonClicked(`mainResponseButton`) =>
      project.currentMetric = Project.ValueMetric
    case ButtonClicked(`errResponseButton`) =>
      project.currentMetric = Project.ErrorMetric
    case ButtonClicked(`gainResponseButton`) =>
      project.currentMetric = Project.GainMetric
    case ButtonClicked(`regionGlyphButton`) =>
      project.showRegion = regionGlyphButton.selected
  }

  // Update which metric we're looking at
  project.currentMetric match {
    case Project.ValueMetric => mainResponseButton.selected = true
    case Project.ErrorMetric => errResponseButton.selected = true
    case Project.GainMetric => gainResponseButton.selected = true
  }

  // Update the glyph controls
  regionGlyphButton.selected = project.showRegion

  override def visible_=(b:Boolean) = {
    super.visible_=(b)
    // See if there are new responses we need to deal with
    if(b && project.newFields.length > 0) {
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
}

