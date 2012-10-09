package tuner.gp

import org.rosuda.JRI.RList
import org.rosuda.JRI.REXP

import tuner.Config
import tuner.R

import numberz.Vector
import numberz.Matrix

import scala.io.Source

object Rgp {
  val DESIGNRVAR = "data.design"
  val ESTIMATERVAR = "data.estimates"
  val MODELRVAR = "gp.fit"
}

class Rgp(designFile:String) {
  
  val (imageDir, sampleFile) = splitFile(designFile)

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
                new Matrix(fm.at("X").asDoubleMatrix),
                new Vector(fm.at("Z").asDoubleArray),
                new Matrix(fm.at("invVarMatrix").asDoubleMatrix),
                paramFields, responseField, errorField)
  }

  // Splits a file into its directory and filename components
  private def splitFile(fname:String) : (String,String) = {
    import java.io.File
    val f = new File(fname)
    (f.getParent, f.getName)
  }
}

