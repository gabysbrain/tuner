package tuner.project

import tuner.DimRanges
import tuner.Region
import tuner.Table

trait Sampler { self:Project =>
  
  val newSamples:Table
  val designSites:Table

  val path:String
  val scriptPath:String

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
}

