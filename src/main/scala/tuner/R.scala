package tuner

//import org.rosuda.REngine.JRI.JRIEngine
//import org.rosuda.REngine.RList
//import org.rosuda.REngine.REXP
//import org.rosuda.JRI.RConsoleOutputStream

import org.rosuda.JRI.Rengine
import org.rosuda.JRI.REXP
import org.rosuda.JRI.RList
import org.rosuda.JRI.RVector
import org.rosuda.JRI.RMainLoopCallbacks

import java.io.PrintStream

object R {

  // Any special arguments to R go in this array
  val RARGS = List("--no-save", "--slave")
  val RequiredPackages:Seq[String] = List("mlegp", "lhs", "labeling")

  print("loading R library...")
  try {
    System.loadLibrary("jri")
  } catch {
    case se:SecurityException => se.printStackTrace
    case le:UnsatisfiedLinkError => le.printStackTrace
    case ne:NullPointerException => ne.printStackTrace
    case e:Exception => e.printStackTrace
  }
  println("done")

  print("initializing R...")
  //val engine = new JRIEngine(RARGS.toArray)
  val engine = new Rengine(RARGS.toArray, false, new RTextConsole)
  if (!engine.waitForR()) {
    System.out.println("Cannot load R")
    throw new Exception("Cannot load R")
  }
  println("done")

  ensurePackages

  //System.setOut(new PrintStream(new RConsoleOutputStream(engine.getRni, 0)))
  //System.setErr(new PrintStream(new RConsoleOutputStream(engine.getRni, 1)))

  def ensurePackages = RequiredPackages.foreach {pkg => installPackage(pkg)}

  def installPackage(pkg:String) = {
    val checkCmd = "is.element('%s', installed.packages()[,1])"
    val instCmd = "install.packages('%s', repos='http://cran.r-project.org')"

    val hasPkg:Boolean = runCommand(checkCmd.format(pkg)).asBool.isTRUE
    if(!hasPkg) {
      println("installing %s".format(pkg))
      runCommand(instCmd.format(pkg))
    }
  }

  def runCommand(cmd:String) : REXP = {
    //println(cmd)
    //val rcmd = engine.parse(cmd, false)
    //val res = engine.eval(rcmd, null, false)
    val res = engine.eval(cmd)
    /*
    if(res.isNull) {
      throw new Exception("ERROR: " + cmd + " failed")
    }
    */
    res
  }

  /*
  def toREXP(values:Array[Double]) = {
    new REXP(values))
  }
  */

  def setVar(exp:REXP, sym:String) = {
    engine.assign(sym, exp)
  }

  def quit() = engine.end

}

