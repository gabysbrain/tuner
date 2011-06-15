package tuner

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

import tuner.util.Prefs

object Config {
  
  def recentProjects : Set[String] = Prefs.list("recentProjects") toSet
  def recentProjects_=(rp:Set[String]) = {
    Prefs.saveList("recentProjects", rp.toList)
  }
  
  val projConfigFilename = "config.json"
  val sampleFilename = "samples.csv"
  val designFilename = "responses.csv"
  val respSampleFilename = "response_samples.csv"
  val imageDirname = "images"

  val gpRScript = getClass.getResource("/sparkle.R").getPath

  val errorField = "stddev"
  val gainField = "estgain"

  val rowField = "rowNum"

  //val estimateSampleDensity = 50
  //val estimateSampleDensity = 25
  val estimateSampleDensity = 20

  val sampleImageSize = 140

  val samplerSplomDims = (400, 400)

  val paretoDims = (289, 289)

  val mainPlotDims = (800, 600)
  val plotSpacing = 5
  val controlPanelHeight = 280

  val samplingRowsPerReq = 4

  //val backgroundColor = 0
  val backgroundColor = 51
  val response1ColorMap = OrangeColorMap
  val response2ColorMap = PurpleColorMap
  val errorColorMap = GrayscaleColorMap
  val gainColorMap = GreenColorMap
  val lineColor = 255
  val filterColor = 151
  val regionAlpha = 0.8f * 255
  val regionColor = 0xAAABCDFC

  // Font stuff
  val fontPath = getClass.getResource("/fonts/MavenPro.otf").getPath
  val smallFontSize = 12

  // Axis configuration stuff
  // Make sure the is big enough to accomodate the labels
  // TODO: do this automatically
  val axisTickSize = 3
  val axisLabelSpace = 3
  val axisSize = smallFontSize + axisTickSize + axisLabelSpace * 2 + 35
  val axisTickDigits = (1,3)
  val axisNumTicks = 4

  // Colorbar configuration stuff
  // TODO: do the width automatically
  val colorbarSpacing = 10
  val colorbarWidth = 77
  val colorbarTickSize = 3
  // vertical, horizontal
  val colorbarLabelSpace = (5, 3)
  val colorbarTickDigits = (1, 3)
  val colorbarHandleSize = (15, 9) // width, height

  val crosshairColor = 0
  val crosshairRadius = 3
  
  val sliderResolution = 1000

  val scatterplotDotSize = 6.5f

  val paretoSampleColor = 0xffDD1C77

  val sampleDotColor = 0xffDD1C77

  val respHistogramSampleDensity = 10000
  val respHistogramPanelDims = (paretoDims._1 - 15, 200)
  val respHistogramBars = 11
  val respHistogramBarFill = Some(255)
  val respHistogramBarStroke = Some(0)
  val respHistogramHandleSize = (colorbarHandleSize._2, colorbarHandleSize._1)

  val sampleLineColor = 255
  val sampleLineDotRadius = scatterplotDotSize / 2
  val sampleLineWidth = 2f
}

