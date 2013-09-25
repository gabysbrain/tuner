package tuner.gp

import org.rosuda.JRI.RList
import org.rosuda.JRI.REXP

import breeze.linalg.{DenseMatrix, DenseVector}

import tuner.Config
import tuner.R
import tuner.Table

import scala.io.Source

object Rgp {
  val DESIGNRVAR = "data.design"
  val ESTIMATERVAR = "data.estimates"
  val MODELRVAR = "gp.fit"
}

class Rgp extends GpBuilder {
  
  // Setup the sparkle gp stuff
  R.runCommand("source('%s')".format(Config.gpRScript))

  def buildModel(design:Table,
                 paramFields:List[String], 
                 responseField:String, 
                 errorField:String) : GpModel = {
    throw new UnsupportedOperationException("not implemented")
  }

  def buildModel(designFile:String,
                 paramFields:List[String], 
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
    new GpModel(DenseVector(fm.at("beta").asDoubleArray),
                DenseVector(fm.at("a").asDoubleArray),
                fm.at("mu").asDouble,
                fm.at("sig2").asDouble,
                new DenseMatrix(fm.at("Z").asDoubleArray.length, 
                                paramFields.length,
                                fm.at("X").asDoubleArray),
                DenseVector(fm.at("Z").asDoubleArray),
                new DenseMatrix(fm.at("Z").asDoubleArray.length, 
                                fm.at("Z").asDoubleArray.length,
                                fm.at("invVarMatrix").asDoubleArray),
                paramFields, responseField, errorField)
  }
}

