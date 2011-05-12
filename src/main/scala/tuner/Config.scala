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

  val estimateSampleDensity = 20

  val mainPlotDims = (800, 600)
  val plotSpacing = 5

  val backgroundColor = 0
  val response1ColorMap = OrangeColorMap
  val response2ColorMap = PurpleColorMap
  val lineColor = 255

  // Font stuff
  val fontPath = getClass.getResource("/fonts/MavenPro.otf").getPath
  val smallFontSize = 9

  // Axis configuration stuff
  val axisTickSize = 5
  val axisLabelSpace = 3
  val axisSize = smallFontSize + axisTickSize + axisLabelSpace * 2 + 35
  val axisTickDigits = (1,3)
}

