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

  val gpRScript = getClass.getResource("/sparkle.R").getPath

  val errorField = "stddev"
  val gainField = "estgain"

  val estimateSampleDensity = 50

  val samplerSplomDims = (400, 400)

  val paretoDims = (289, 289)

  val mainPlotDims = (800, 600)
  val plotSpacing = 5

  val backgroundColor = 0
  val response1ColorMap = OrangeColorMap
  val response2ColorMap = PurpleColorMap
  val lineColor = 255
  val filterColor = 180

  // Font stuff
  val fontPath = getClass.getResource("/fonts/MavenPro.otf").getPath
  val smallFontSize = 10

  // Axis configuration stuff
  // Make sure the is big enough to accomodate the labels
  // TODO: do this automatically
  val axisTickSize = 3
  val axisLabelSpace = 3
  val axisSize = smallFontSize + axisTickSize + axisLabelSpace * 2 + 35
  val axisTickDigits = (1,3)
  println("axis size: " + axisSize)

  // Colorbar configuration stuff
  // TODO: do the width automatically
  val colorbarSpacing = 10
  val colorbarWidth = 70
  val colorbarTickSize = 3
  // vertical, horizontal
  val colorbarLabelSpace = (5, 3)
  val colorbarTickDigits = (1, 3)
  val colorbarHandleSize = (15, 9) // width, height

  val crosshairColor = 0
  val crosshairRadius = 3
  
  val regionColor = 0xAAABCDFC

  val regionMaskAlpha = 0.2f

  val sliderResolution = 1000

  val scatterplotDotSize = 6.5f

  val paretoSampleColor = 0xffDD1C77
}

