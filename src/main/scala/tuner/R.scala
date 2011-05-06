package tuner

import org.rosuda.REngine.JRI.JRIEngine
import org.rosuda.REngine.RList
import org.rosuda.REngine.REXP

object R {

  // Any special arguments to R go in this array
  val RARGS = List("--no-save")

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
  val engine = new JRIEngine(RARGS.toArray)
  println("done")

  def runCommand(cmd:String) : REXP = {
    val rcmd = engine.parse(cmd, false)
    val res = engine.eval(rcmd, null, false)
    /*
    if(res == null || res.isNull) {
      println("ERROR: " + cmd + " failed")
    }
    */
    res
  }

  def setVar(exp:REXP, sym:String) = {
    engine.assign(sym, exp)
  }

  def quit() = engine.close

}

