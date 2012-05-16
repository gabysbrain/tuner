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

import tuner.error._

object Rapp {
  val MacRHome = "/Library/Frameworks/R.framework/Resources"
  val LinRHome = "/usr/lib64/R"
  val WinRegKey = "HKLM\\Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\"

  def installPackage(pkg:String) :Unit = cmd match {
    case Some(app) =>
      val instCmd = "install.packages('%s', repos='http://cran.r-project.org')"
      val cmd = app + " -e \"" + instCmd + "\""
      println(cmd)
      val proc = Runtime.getRuntime.exec(cmd)
      println("cmd finished with " + proc.waitFor)
    case None => throw new MissingRException(null)
  }

  def cmd : Option[String] = Rapp.path map {path =>
    val appName = OS.detect match {
      case OS.Mac => "bin/R64"
      case OS.Win => "bin\\x64\\R.exe"
      case OS.Unix => "bin/R"
    }
    new java.io.File(path, appName).getAbsolutePath
  }

  /**
   * Returns the path to the R installation
   */
  def path : Option[String] = {
    val env = System.getenv("RHOME")
    if(env != null) {
      println(env)
      Some(env)
    } else {
      OS.detect match {
        case OS.Mac  => macPath
        case OS.Win  => winPath
        case OS.Unix => linPath
      }
    }
  }

  /**
   * Makes sure the R path is ok
   */
  def pathOk : Boolean = path exists {p =>
    val home = new java.io.File(p)
    if(home.exists && home.isDirectory) true
    else                                false
  }

  def macPath : Option[String] = Some(MacRHome)

  def winPath : Option[String] = {
    val programs = WindowsRegistry.readRegistry(WinRegKey).split("\n")
    programs.find {k => k.toLowerCase.contains("r for win")} map {rkey =>
      WindowsRegistry.readRegistry(rkey.trim, "InstallLocation")
    }
  }

  def linPath : Option[String] = Some(LinRHome)

  def jriOk : Boolean = {
    try {
      System.loadLibrary("jri")
      true
    } catch {
      case le:UnsatisfiedLinkError => false
    }
  }

}

object R {

  // Any special arguments to R go in this array
  val RARGS = List("--no-save", "--slave")
  val RequiredPackages:Seq[String] = List("mlegp", "lhs", "labeling")

  print("loading R library...")
  try {
    System.loadLibrary("jri")
  } catch {
    case se:SecurityException => 
      throw new RInitException("Could not load jri due to security", se)
    case le:UnsatisfiedLinkError => 
      throw new MissingJriException(le)
    case ne:NullPointerException => 
      throw new RInitException("Could not load jri due to null pointer", ne)
    case e:Exception => 
      throw new RInitException("Could not load jri. unknown reason", e)
  }
  println("done")

  print("initializing R...")
  val engine = new Rengine(RARGS.toArray, false, new RTextConsole)
  if (!engine.waitForR()) {
    // A common cause is not setting the R_HOME variable
    if(System.getenv("R_HOME") == null) {
      throw new RInitException("Cannot load R. R_HOME not set.")
    } else {
      throw new RInitException("Cannot load R")
    }
  }
  println("done")

  ensurePackages

  //System.setOut(new PrintStream(new RConsoleOutputStream(engine.getRni, 0)))
  //System.setErr(new PrintStream(new RConsoleOutputStream(engine.getRni, 1)))

  /**
   * Make sure all the required packages are installed
   */
  def ensurePackages = RequiredPackages.foreach {pkg => installPackage(pkg)}

  def missingPackages = RequiredPackages.filter {pkg => !hasPackage(pkg)}

  def hasPackage(pkg:String) : Boolean = {
    val checkCmd = "is.element('%s', installed.packages()[,1])"
    runCommand(checkCmd.format(pkg)).asBool.isTRUE
  }

  def installPackage(pkg:String) : Unit = {
    val instCmd = "install.packages('%s', repos='http://cran.r-project.org')"

    if(!hasPackage(pkg)) {
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

