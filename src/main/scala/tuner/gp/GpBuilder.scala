package tuner.gp

abstract class GpBuilder(designFile:String) {

  val (imageDir, sampleFile) = splitFile(designFile)
  
  def buildModel(paramFields:List[String], 
                 responseField:String, 
                 errorField:String) : GpModel

  // Splits a file into its directory and filename components
  private def splitFile(fname:String) : (String,String) = {
    import java.io.File
    val f = new File(fname)
    (f.getParent, f.getName)
  }
}

