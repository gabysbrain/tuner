package tuner.gui

import tuner.Config
import tuner.DimRanges
import tuner.GpModel
import tuner.Matrix2D
import tuner.Project
import tuner.SpecifiedColorMap

class MainPlotPanel(project:Project, resp1:Option[String], resp2:Option[String]) 
    extends P5Panel(Config.mainPlotDims._1, Config.mainPlotDims._2) {

  type PlotInfoMap = Map[(String,String), ContinuousPlot]

  var zoomDims = new DimRanges(project.inputs.ranges)
  var currentSlice:Map[String,Float] = project.inputFields.map {fld =>
    val rng = zoomDims.range(fld)
    (fld, (rng._1 + rng._2) / 2f)
  } toMap

  val resp1Info:Option[(String,GpModel,PlotInfoMap)] = resp1 match {
    case Some(r1) => project.gpModels match {
      case Some(gpm) => 
        val model = gpm(r1)
        val cm = new SpecifiedColorMap(Config.response1ColorMap, 
                                       model.funcMin, 
                                       model.funcMax)
        Some(r1, model, createPlots(cm))
      case None      => None
    }
    case None     => None
  }

  val resp2Info:Option[(String,GpModel,PlotInfoMap)] = resp2 match {
    case Some(r2) => project.gpModels match {
      case Some(gpm) => 
        val model = gpm(r2)
        val cm = new SpecifiedColorMap(Config.response2ColorMap, 
                                       model.funcMin, 
                                       model.funcMax)
        Some(r2, model, createPlots(cm))
      case None      => None
    }
    case None     => None
  }

  def sortedDims : List[String] = zoomDims.dimNames.sorted

  def plotData(model:GpModel,
               d1:(String,(Float,Float)), 
               d2:(String,(Float,Float)), 
               slice:Map[String,Float]) : Matrix2D = {
    model.sampleSlice(d1, d2, slice.toList)._1._2
  }

  def draw = {
    // response1 goes in the lower left
    resp1Info foreach {case (field, model, plots) =>
      drawResponse(field, model, plots, {(x,y) => x < y})
    }

    // response2 goes in the upper right
    resp2Info foreach {case (field, model, plots) =>
      drawResponse(field, model, plots, {(x,y) => x > y})
    }
  }

  def drawResponse(field:String, model:GpModel, plots:PlotInfoMap, compare:(String,String) => Boolean) = {
    val sliceSize = (height - (zoomDims.length+1) * Config.plotSpacing) /
                    (zoomDims.length)

    sortedDims.foldLeft(Config.plotSpacing) {case (xPos, xFld) =>
      sortedDims.foldLeft(Config.plotSpacing) {case (yPos, yFld) =>
        if(compare(xFld, yFld)) {
          val data = 
            plotData(model, 
                     (xFld, zoomDims.range(xFld)), 
                     (yFld, zoomDims.range(yFld)),
                     currentSlice)
          // Sometimes xFld and yFld aren't in order
          val (f1, f2) = if(xFld < yFld) (xFld, yFld)
                         else            (yFld, xFld)
          val plot = plots((f1, f2))
          plot.draw(this, xPos, yPos, sliceSize, sliceSize, data)
        }
        yPos + Config.plotSpacing + sliceSize
      }
      xPos + Config.plotSpacing + sliceSize
    }
  }

  private def createPlots(cm:SpecifiedColorMap) : PlotInfoMap = {
    project.inputFields.flatMap({fld1 =>
      project.inputFields.flatMap({fld2 =>
        if(fld1 < fld2) {
          Some(((fld1, fld2), 
            new ContinuousPlot(zoomDims.min(fld1), zoomDims.max(fld1),
                               zoomDims.min(fld2), zoomDims.max(fld2),
                               cm)))
        } else {
          None
        }
      })
    }).toMap
  }
}

