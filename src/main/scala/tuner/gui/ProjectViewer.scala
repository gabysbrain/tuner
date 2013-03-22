package tuner.gui

import scala.swing.BorderPanel
import scala.swing.BoxPanel
import scala.swing.ButtonGroup
import scala.swing.CheckBox
import scala.swing.Dialog
import scala.swing.FlowPanel
import scala.swing.Label
import scala.swing.Orientation
import scala.swing.RadioButton
import scala.swing.SplitPane
import scala.swing.Swing
import scala.swing.TablePanel
import scala.swing.event.ButtonClicked
import scala.swing.event.DialogClosing
import scala.swing.event.ValueChanged
import scala.swing.event.WindowClosing

import tuner.Config
import tuner.ViewInfo
import tuner.gui.event.AddSamples
import tuner.gui.event.CandidateChanged
import tuner.gui.event.HistoryAdd
import tuner.gui.event.SliceChanged
import tuner.gui.event.ViewChanged
import tuner.project.Saved
import tuner.project.Viewable

/**
 * The main interaction window in Tuner
 */
class ProjectViewer(project:Viewable) extends Window(project) {
  
  title = project.name
  val myMenu = new MainMenu(project) {
    importSamples.action = MainMenu.ImportSamplesAction(project)
    enabled = true
  }
  menuBar = myMenu

  // only use the native opengl version if the system can handle it
  val mainPlotPanel = new JoglMainPlotPanel(project)
  //val mainPlotPanel = new ProcessingMainPlotPanel(project)

  val visControlPanel = new VisControlPanel(project.viewInfo)

  val controlPanel = new ControlPanel(project)

  val paretoPanel = new ParetoPanel(project) {
    border = Swing.TitledBorder(border, "Pareto")
    minimumSize = new java.awt.Dimension(Config.paretoDims._1, 
                                         Config.paretoDims._2)
    maximumSize = preferredSize
  }

  contents = new TablePanel(List(305,TablePanel.Size.Fill), 
                            List(TablePanel.Size.Fill)) {
  
    val histogramPanel = new ResponseStatsPanel(project) {
      border = Swing.TitledBorder(border, "Response Histograms")
    }
  
    val leftPanel = new BoxPanel(Orientation.Vertical) {
      contents += paretoPanel
      contents += visControlPanel
      contents += histogramPanel
    }

    val rightPanel = new SplitPane(Orientation.Horizontal) {
      /*
      contents += mainPlotPanel
      contents += controlPanel
      */
      topComponent = mainPlotPanel
      bottomComponent = controlPanel
    }

    layout(leftPanel) = (0,0)
    layout(rightPanel) = (1,0)
  }

  listenTo(mainPlotPanel)
  listenTo(visControlPanel)
  listenTo(paretoPanel)
  listenTo(controlPanel.controlsTab)
  listenTo(controlPanel.historyTab)
  listenTo(controlPanel.candidatesTab)
  listenTo(controlPanel.localTab)
  listenTo(myMenu.importSamples)

  val controlsTab = controlPanel.controlsTab
  val localTab = controlPanel.localTab
  val importSamplesItem = myMenu.importSamples
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
    case ViewChanged(`visControlPanel`) =>
      mainPlotPanel.redraw
    case AddSamples(_) => 
      openSamplerDialog
    case HistoryAdd(_, sliceInfo) =>
      project.history.add(sliceInfo)
      controlPanel.historyTab.updateTable
    case ValueChanged(`controlsTab`) =>
      mainPlotPanel.redraw
    case ValueChanged(`localTab`) =>
      mainPlotPanel.redraw
    case ButtonClicked(`importSamplesItem`) =>
      this.close
    case WindowClosing(_) => 
      paretoPanel.stop
      controlPanel.infoTab.sampleImagePanel.stop

      project match {
        case s:Saved => s.save()
        case _ =>
      }
  }

  private def openSamplerDialog = {
    val samplerDialog = new SamplerDialog(project, this)
    listenTo(samplerDialog)
    reactions += {
      case DialogClosing(`samplerDialog`, result) => result match {
        case Dialog.Result.Ok => 
          project.save()
          openNextStage
        case Dialog.Result.Cancel =>
          project.newSamples.clear
      }
    }
    samplerDialog.open
  }

}

