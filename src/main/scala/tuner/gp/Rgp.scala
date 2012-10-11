package tuner.gp

import org.rosuda.JRI.RList
import org.rosuda.JRI.REXP

import tuner.Config
import tuner.R

import numberz.Matrix
import numberz.Vector

import scala.io.Source

object Rgp {
  val DESIGNRVAR = "data.design"
  val ESTIMATERVAR = "data.estimates"
  val MODELRVAR = "gp.fit"
}

class Rgp(designFile:String) extends GpBuilder(designFile) {
  
  // Setup the sparkle gp stuff
  R.runCommand("source('%s')".format(Config.gpRScript))

  def buildModel(paramFields:List[String], 
                 responseField:String, 
                 errorField:String) : GpModel = {
    print("building model...")

    // Read the design file
    val rDesignFile = designFile.replace("\\", "/")
    R.runCommand("%s <- read.design('%s')".format(Rgp.DESIGNRVAR, rDesignFile))
    // Create an r vector of all the strings
    val rvect = "c(" + paramFields.map("'"+_+"'").reduceLeft(_+","+_) + ")"
    //println("params: " + rvect)
    val fit = R.runCommand("%s <- fit.model(%s, %s, '%s')".
      format(Rgp.MODELRVAR, Rgp.DESIGNRVAR, rvect, responseField))
    println("done")

    val fm = fit.asList
    new GpModel(new Vector(fm.at("beta").asDoubleArray),
                new Vector(fm.at("a").asDoubleArray),
                fm.at("mu").asDouble,
                fm.at("sig2").asDouble,
                Matrix.fromColumnMajor(fm.at("X").asDoubleMatrix),
                new Vector(fm.at("Z").asDoubleArray),
                Matrix.fromColumnMajor(fm.at("invVarMatrix").asDoubleMatrix),
                paramFields, responseField, errorField)
  }
}

