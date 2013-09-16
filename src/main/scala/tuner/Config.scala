package tuner

import java.awt.Toolkit

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

import tuner.util.Prefs
import tuner.util.ResourceLoader

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

  val smallScreen = Toolkit.getDefaultToolkit.getScreenSize.height < 800

  val gpRScript = 
    ResourceLoader.fileResource("/sparkle.R").replace("\\", "/")

  val errorField = "stddev"
  val gainField = "estgain"

  val rowField = "rowNum"

  val maxEstimateSamples = if(smallScreen) 4000 else 15000
  val maxSampleSqDistance = 1e-9

  val sampleImageSize = 120

  val samplerSplomDims = (400, 400)

  val paretoDims = (289, 289)

  val mainPlotDims = if(smallScreen) {
    (600, 380)
  } else {
    (800, 600)
  }
  //val mainPlotDims = (600, 380)
  val plotSpacing = 5
  val controlPanelHeight = 220

  val samplingRowsPerReq = 4

  val backgroundColor = 51
  val response1ColorMap = OrangeColorMap
  val response2ColorMap = PurpleColorMap
  val errorColorMap = BlueColorMap
  val gainColorMap = GreenColorMap
  val sampleColorMap = RedColorMap
  val lineColor = 255
  val filterColor = Color(151)
  val regionAlpha = 0.8f * 255
  val regionColor = 0xAAABCDFC

  // Font stuff
  val fontPath = ResourceLoader.fileResource("/fonts/MavenPro.otf")
  val smallFontSize = 12

  // Axis configuration stuff
  // Make sure the is big enough to accomodate the labels
  // TODO: do this automatically
  val axisTickSize = 3
  val axisLabelSpace = 3
  val axisSize = smallFontSize + axisTickSize + axisLabelSpace * 2 + 35
  val axisTickDigits = (1,3)
  val axisNumTicks = 5

  // Colorbar configuration stuff
  // TODO: do the width automatically
  val colorbarSpacing = 10
  val colorbarWidth = 81
  val colorbarTickSize = 3
  // vertical, horizontal
  val colorbarLabelSpace = (5, 3)
  val colorbarTickDigits = (1, 3)
  val colorbarHandleSize = (15, 9) // width, height
  val colorbarTicks = 3

  val crosshairColor = 0
  val crosshairRadius = 3
  
  val sliderResolution = 1000

  val scatterplotDotSize = 6.5f

  val paretoSampleColor = 0xffDD1C77
  val paretoInactiveSampleColor = 0xffDDDDDD

  val sampleDotColor = 0xffDD1C77

  val numericSampleDensity = 10000

  val respHistogramPanelDims = (paretoDims._1 - 50, 130)
  val respHistogramBars = 11
  val respHistogramBarFill = Some(255)
  val respHistogramBarStroke = Some(0)
  val respHistogramHandleSize = (colorbarHandleSize._2, colorbarHandleSize._1)
  val respHistogramFramerate = 30

  val sampleLineColor = 255
  val sampleLineDotRadius = scatterplotDotSize / 2
  val sampleLineWidth = 2f
}

