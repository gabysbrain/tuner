package tuner.gp

import tuner.Table

trait GpBuilder {

  def buildModel(designFile:String,
                 paramFields:List[String], 
                 responseField:String, 
                 errorField:String) : GpModel

  def buildModel(design:Table,
                 paramFields:List[String], 
                 responseField:String, 
                 errorField:String) : GpModel


  // Splits a file into its directory and filename components
  private def splitFile(fname:String) : (String,String) = {
    import java.io.File
    val f = new File(fname)
    (f.getParent, f.getName)
  }
}

