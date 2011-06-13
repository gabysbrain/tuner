package tuner

import net.liftweb.json.JsonParser._
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._

import java.io.File
import java.io.FileWriter
import java.util.Date

import scala.io.Source

import tuner.util.Density2D

// Internal config for matching with the json stuff
case class InputSpecification(name:String, minRange:Float, maxRange:Float)
case class OutputSpecification(name:String, minimize:Boolean)
case class GpSpecification(
  responseDim:String,
  dimNames:List[String],
  thetas:List[Double],
  alphas:List[Double],
  mean:Double,
  sigma2:Double,
  designMatrix:List[List[Double]],
  responses:List[Double],
  invCorMtx:List[List[Double]]
)
case class ProjConfig(
  name:String,
  scriptPath:String,
  inputs:List[InputSpecification],
  outputs:List[OutputSpecification],
  ignoreFields:List[String],
  gpModels:List[GpSpecification],
  buildInBackground:Boolean,
  currentVis:VisInfo,
  currentRegion:RegionSpecification,
  history:Option[HistorySpecification]
)

object Project {
  def recent : List[Project] = {
    Config.recentProjects map {Project.fromFile(_)} toList
  }

  def fromFile(path:String) = {
    new Project(Some(path))
  }

  sealed trait Status {
    def statusString:String
  }
  case object Ok extends Status {
    def statusString = "Ok"
  }
  case object NeedsInitialSamples extends Status {
    def statusString = "Needs Initial Samples"
  }
  case class RunningSamples(numDone:Int, total:Int) extends Status {
    def statusString = numDone + "/" + total + " Samples Done"
  }
  case object BuildingGp extends Status {
    def statusString = "Building GP"
  }

  sealed trait MetricView
  case object ValueMetric extends MetricView
  case object ErrorMetric extends MetricView
  case object GainMetric extends MetricView
}

class Project(var path:Option[String]) {

  import Project._

  def this() = this(None)

  // Serializers to get the json parser to work
  implicit val formats = net.liftweb.json.DefaultFormats

  var config = path map {p =>
    val projConfigPath = p + "/" + Config.projConfigFilename
    val json = parse(Source.fromFile(projConfigPath).mkString)
    json.extract[ProjConfig]
  }

  var name : String = config match {
    case Some(c) => c.name
    case None    => "New Project"
  }
  var scriptPath : Option[String] = config map {_.scriptPath}
  def modificationDate : Date = new Date

  var _inputs : DimRanges = config match {
    case Some(c) => 
      new DimRanges(c.inputs map {x => 
        (x.name, (x.minRange, x.maxRange))
      } toMap)
    case None    => new DimRanges(Nil.toMap)
  }

  val newSamples = path match {
    case Some(p) => try {
        val sampleFilename = p + "/" + Config.sampleFilename
        Table.fromCsv(sampleFilename)
      } catch {
        case fnf:java.io.FileNotFoundException => new Table
      }
    case None => new Table
  }

  var designSites = path match {
    case Some(p) =>
      val designSiteFile = p + "/" + Config.designFilename
      try {
        Some(Table.fromCsv(designSiteFile))
      } catch {
        case fnf:java.io.FileNotFoundException => None
      }
    case None => None
  }

  var responses : List[(String,Boolean)] = config match {
    case Some(c) => c.outputs map {rf => (rf.name, rf.minimize)}
    case None => Nil
  }

  var ignoreFields : List[String] = config match {
    case Some(c) => c.ignoreFields
    case None => Nil
  }

  // The visual controls
  val viewInfo = config match { 
    case Some(c) => ViewInfo.fromJson(this, c.currentVis)
    case None    => new ViewInfo(this)
  }

  val gpModels : Option[Map[String,GpModel]] = path match {
    case Some(p) =>
      if(designSites.isDefined) {
        val designSiteFile = p + "/" + Config.designFilename
        val gp = new Rgp(designSiteFile)
        Some(responseFields.map {fld => (fld, loadGpModel(gp, fld))} toMap)
      } else {
        None
      }
    case None => None
  }

  var _region:Region = config match {
    case Some(c) => Region.fromJson(c.currentRegion, this)
    case None    => Region(Region.Box, this)
  }

  val history:HistoryManager = config match {
    case Some(c) => c.history match {
      case Some(hc) => HistoryManager.fromJson(hc)
      case None     => new HistoryManager
    }
    case None => new HistoryManager
  }

  val candidateGenerator = new CandidateGenerator(this)

  // See if we should do offline stuff in the background
  var _buildInBackground:Boolean = config match {
    case Some(c) => c.buildInBackground
    case None    => false
  }

  val previewImages:Option[PreviewImages] = path match {
    case Some(p) => loadImages(p)
    case None    => None
  }

  // See if we should start running some samples
  var sampleRunner:Option[SampleRunner] = None 
  if(_buildInBackground) runSamples

  // Also set up a table of samples from each gp model
  lazy val modelSamples:Table = path match {
    case Some(p) => loadResponseSamples(p)
    case None    => new Table
  }

  // Save any gp models that got updated
  path.foreach(_ => save(savePath))

  def status : Project.Status = {
    if(newSamples.numRows == 0 && !designSites.forall(_.numRows!=0)) {
      Project.NeedsInitialSamples
    } else if(newSamples.numRows > 0) {
      sampleRunner match {
        case Some(sr) => 
          Project.RunningSamples(sr.completedSamples, sr.totalSamples)
        case None     =>
          Project.RunningSamples(0, newSamples.numRows)
      }
    } else if(!gpModels.isDefined) {
      Project.BuildingGp
    } else {
      Project.Ok
    }
  }

  def buildInBackground = _buildInBackground
  def buildInBackground_=(b:Boolean) = {
    _buildInBackground = b
  }

  def runSamples = {
    // only run if we aren't running something
    if(!sampleRunner.isDefined && newSamples.numRows > 0) {
      val runner = new SampleRunner(this)
      runner.start
      sampleRunner = Some(runner)
    }
  }

  def inputFields : List[String] = inputs.dimNames.sorted
  def responseFields : List[String] = responses.map(_._1).sorted

  def inputs : DimRanges = _inputs
  def inputs_=(dr:DimRanges) = {
    _inputs = dr
    region = Region(Region.Box, this)
    _inputs.dimNames.foreach {fld =>
      val (mn, mx) = _inputs.range(fld)
      region.setRadius(fld, (mn+mx)/2)
    }
  }

  def region : Region = _region
  def region_=(r:Region) = {
    _region = r
  }

  def newFields : List[String] = {
    val knownFields : Set[String] = 
      (responseFields ++ ignoreFields ++ inputFields).toSet
    designSites.get.fieldNames.filter {fn => !knownFields.contains(fn)}
  }

  def addSamples(n:Int, range:DimRanges, method:Sampler.Method) : Unit = {
    // TODO: find a better solution than just ignoring the missing inputs
    if(n > 0) {
      method(range, n, {v => newSamples.addRow(v)})
      println(n + " samples generated")
    }
  }

  def addSamples(n:Int, method:Sampler.Method) : Unit = {
    addSamples(n, inputs, method)
  }

  def newSamples(n:Int, range:DimRanges, method:Sampler.Method) : Unit = {
    newSamples.clear
    addSamples(n, range, method)
  }

  /**
   * Clears out the sample table then adds the samples
   */
  def newSamples(n:Int, method:Sampler.Method) : Unit = {
    newSamples(n, inputs, method)
  }

  def modeledSamplesSize = gpModels match {
    case None => 0
    case Some(models) => 
      val model = models.head._2
      model.design.size
  }

  def updateCandidates(newValues:List[(String,Float)]) = {
    candidateGenerator.update(newValues)
  }

  def candidates = candidateGenerator.candidates

  def candidateFilter = candidateGenerator.currentFilter

  def closestSample(point:List[(String,Float)]) : List[(String,Float)] = {
    def ptDist(tpl:Table.Tuple) : Double = {
      val diffs = point.map {case (fld, v) =>
        math.pow(tpl.getOrElse(fld, Float.MaxValue) - v, 2)
      }
      math.sqrt(diffs.sum)
    }
    designSites match {
      case Some(t) => t.data.foldLeft(Double.MaxValue, t.tuple(0))((mi,r) => {
        val dist = ptDist(r)
        if(dist < mi._1) (dist, r)
        else             mi
      })._2.toList
      case None => Nil
    }
  }

  def estimatePoint(point:List[(String,Float)]) 
        : Map[String,(Float,Float,Float)] = {
    gpModels match {
      case Some(models) => models.map {case (fld, model) =>
        val (est, err) = model.runSample(point)
        (fld -> (est.toFloat, err.toFloat, 
                 model.calcExpectedGain(est.toFloat, err.toFloat)))
      }
      case None         => Map()
    }
  }

  def savePath : String = path.get

  def save(savePath:String) = {
    path = Some(savePath)

    val json = (
      ("name" -> name) ~
      ("scriptPath" -> scriptPath) ~
      ("inputs" -> inputs.dimNames.map {dn =>
        ("name" -> dn) ~
        ("minRange" -> inputs.min(dn)) ~
        ("maxRange" -> inputs.max(dn))}
      ) ~
      ("outputs" -> responses.map {rf => 
        ("name" -> rf._1) ~
        ("minimize" -> rf._2)}
      ) ~
      ("ignoreFields" -> ignoreFields) ~
      ("buildInBackground" -> buildInBackground) ~
      ("gpModels" -> (gpModels match {
        case Some(models) => models.map {case (fld, model) =>
          ("responseDim" -> fld) ~
          ("dimNames" -> model.dims) ~
          ("thetas" -> model.thetas) ~
          ("alphas" -> model.alphas) ~
          ("mean" -> model.mean) ~
          ("sigma2" -> model.sig2) ~
          ("designMatrix" -> model.design.map(_.toList).toList) ~
          ("responses" -> model.responses.toList) ~
          ("invCorMtx" -> model.rInverse.map(_.toList).toList)
        } toList
        case None         => Nil
      })) ~
      ("currentVis" -> viewInfo.toJson) ~
      ("currentRegion" -> region.toJson) ~
      ("history" -> history.toJson)
    )

    // Ensure that the project directory exists
    var pathDir = new File(savePath).mkdir

    val jsonPath = savePath + "/" + Config.projConfigFilename
    val outFile = new FileWriter(jsonPath)
    outFile.write(pretty(render(json)))
    outFile.close

    // Also save the samples
    val sampleName = savePath + "/" + Config.sampleFilename
    newSamples.toCsv(sampleName)

    // Also save the design points
    designSites.foreach {ds =>
      val designName = savePath + "/" + Config.designFilename
      ds.toCsv(designName)
    }

    if(modelSamples.numRows > 0) {
      val filepath = savePath + "/" + Config.respSampleFilename
      modelSamples.toCsv(filepath)
    }
  }

  private def loadGpModel(factory:Rgp, field:String) : GpModel = {
    val gpConfig:Option[GpSpecification] = config match {
      case Some(c) => c.gpModels.find(_.responseDim==field)
      case None    => None
    }
    gpConfig match {
      case Some(c) => 
        new GpModel(c.thetas, c.alphas, c.mean, c.sigma2,
                    c.designMatrix.map {_.toArray} toArray, 
                    c.responses.toArray,
                    c.invCorMtx.map {_.toArray} toArray, 
                    c.dimNames, c.responseDim, Config.errorField)
      case None    => 
        println("building model for " + field)
        factory.buildModel(inputFields, field, Config.errorField)
    }
  }

  def randomSample2dResponse(resp1Dim:(String,(Float,Float)), 
                             resp2Dim:(String,(Float,Float))) = {

    Density2D.density(modelSamples, Config.estimateSampleDensity*2, 
                      resp2Dim, resp1Dim)
  }

  private def loadResponseSamples(path:String) : Table = gpModels match {
    case Some(gpm) =>
      // First try to load up an old file
      val samples = try {
        val filepath = path + "/" + Config.respSampleFilename
        Table.fromCsv(filepath)
      } catch {
        case e:java.io.FileNotFoundException => 
          Sampler.lhc(inputs, Config.respHistogramSampleDensity)
      }
      gpm.foldLeft(samples) {case (tbl, (fld, model)) =>
        if(!tbl.fieldNames.contains(fld))
          gpm(fld).sampleTable(tbl)
        else
          tbl
      }
    case None => new Table
  }

  private def loadImages(path:String) : Option[PreviewImages] = {
    (gpModels, designSites) match {
      case (Some(gpm), Some(ds)) => 
        if(!gpm.isEmpty) {
          val model = gpm.values.head
          val imagePath = path + "/" + Config.imageDirname
          try {
            Some(new PreviewImages(model, imagePath, ds))
          } catch {
            case e:java.io.FileNotFoundException => 
              //e.printStackTrace
              println("Could not find images, disabling")
              None
          }
        } else {
          None
        }
      case _ => None
    }
  }
}

