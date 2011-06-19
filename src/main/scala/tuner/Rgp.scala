package tuner

import org.rosuda.REngine.JRI.JRIEngine
import org.rosuda.REngine.RList
import org.rosuda.REngine.REXP

import scala.io.Source

object Rgp {
  val DESIGNRVAR = "data.design"
  val ESTIMATERVAR = "data.estimates"
  val MODELRVAR = "gp.fit"

  /*
  def main(args:Array[String]) = {
    val gp = new Rgp("data/temp_design.txt")
    val model = gp.buildModel(List("alpha","sigma"), "dice", "dace_stddev")
  }
  */
}

class Rgp(designFile:String) {
  
  val (imageDir, sampleFile) = splitFile(designFile)

  // Setup the sparkle gp stuff
  /*
  val scriptFile = Source.fromURL(getClass.getResource(Config.gpRScript))
  scriptFile.getLines.foreach{line =>
    println(line)
    R.runCommand(line)
  }
  */
  R.runCommand("source('%s')".format(Config.gpRScript))

  // Load in the design table and then do any scaling we need
  /*
  R.runCommand("%s <- read.design('%s')".format(Rgp.DESIGNRVAR, designFile))
  logFields.foreach {fld =>
    R.runCommand("%s <- log.column(%s, '%s')".
      format(Rgp.DESIGNRVAR, Rgp.DESIGNRVAR, fld))
  }
  */

  // Save the adjusted design file
  //R.runCommand("save.table(%s, '%s')".format(Rgp.DESIGNRVAR, adjDesignFile))

  def buildModel(paramFields:List[String], 
                 responseField:String, 
                 errorField:String) : GpModel = {
    print("building model...")
    // Read the design file

    R.runCommand("%s <- read.design('%s')".format(Rgp.DESIGNRVAR, designFile))
    // Create an r vector of all the strings
    val rvect = "c(" + paramFields.map("'"+_+"'").reduceLeft(_+","+_) + ")"
    //println("params: " + rvect)
    val fit = R.runCommand("%s <- fit.model(%s, %s, '%s')".
      format(Rgp.MODELRVAR, Rgp.DESIGNRVAR, rvect, responseField))
    println("done")

    /*
    nsamp:Int => {
      print("running estimates...")
      R.runCommand("%s <- take.estimates(%s, %d)".
        format(ESTIMATERVAR, MODELRVAR, nsamp))
      R.runCommand("save.table(%s, '%s')".format(ESTIMATERVAR, estimateFile))
      println("done")
      estimateFile
    }
    */
    new GpModel(fit.asList, paramFields, responseField, errorField)
  }

  // Splits a file into its directory and filename components
  private def splitFile(fname:String) : (String,String) = {
    import java.io.File
    val f = new File(fname)
    (f.getParent, f.getName)
  }
}

