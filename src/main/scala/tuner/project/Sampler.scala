package tuner.project

import tuner.Config
import tuner.DimRanges
import tuner.Region
import tuner.Table
import tuner.util.Path

/**
 * A type of project that can have additional sample points added to it
 */
trait Sampler { self:Project =>
  
  // New samples to run through the simulation
  val newSamples:Table
  // Already run sample points (have outputs from simulation)
  val designSites:Table

  val path:String
  val scriptPath:String

  def saveSampleTables(savePath:String) = {
    // Also save the samples
    val sampleName = Path.join(savePath, Config.sampleFilename)
    newSamples.toCsv(sampleName)

    // Also save the design points
    val designName = Path.join(savePath, Config.designFilename)
    designSites.toCsv(designName)
  }

  /**
   * Information about the input dimension ranges for sampling
   */ 
  def sampleRanges : DimRanges

  def addSamples(n:Int, range:DimRanges, method:tuner.Sampler.Method) : Unit = {
    // TODO: find a better solution than just ignoring the missing inputs
    if(n > 0) {
      method(range, n, {v => newSamples.addRow(v)})
      //println(n + " samples generated")
    }
  }

  def addSamples(n:Int, method:tuner.Sampler.Method) : Unit = {
    addSamples(n, inputs, method)
  }

  def newSamples(n:Int, range:DimRanges, method:tuner.Sampler.Method) : Unit = {
    newSamples.clear
    addSamples(n, range, method)
  }

  /**
   * Clears out the sample table then adds the samples
   */
  def newSamples(n:Int, method:tuner.Sampler.Method) : Unit = {
    newSamples(n, inputs, method)
  }

  def importSamples(file:java.io.File) = {
    val newDesTbl = Table.fromCsv(file.getAbsolutePath)
    for(r <- 0 until newDesTbl.numRows) {
      val tpl = newDesTbl.tuple(r)
      designSites.addRow(tpl.toList)
    }
  }
}

