package tuner.project

import net.liftweb.json._
import net.liftweb.json.Extraction._

import java.io.File
import java.io.FileWriter
import java.util.Date

import scala.actors.Actor
import scala.actors.Actor._
import scala.collection.immutable.SortedMap
import scala.collection.mutable.ArrayBuffer
import scala.io.Source

import tuner.CandidateGenerator
import tuner.Config
import tuner.ConsoleLine
import tuner.DimRanges
import tuner.GpModel
import tuner.GpSpecification
import tuner.HistoryManager
import tuner.HistorySpecification
import tuner.Matrix2D
import tuner.PreviewImages
import tuner.Progress
import tuner.ProgressComplete
import tuner.Region
import tuner.RegionSpecification
import tuner.Rgp
import tuner.SampleRunner
import tuner.SamplesCompleted
import tuner.SamplingComplete
import tuner.SamplingError
import tuner.Table
import tuner.ViewInfo
import tuner.VisInfo
import tuner.error.ProjectLoadException
import tuner.util.Density2D
import tuner.util.Path

// Internal config for matching with the json stuff
case class InputSpecification(name:String, minRange:Float, maxRange:Float)
case class OutputSpecification(name:String, minimize:Boolean)
case class ProjConfig(
  name:String,
  scriptPath:String,
  inputs:List[InputSpecification],
  var outputs:List[OutputSpecification],
  var ignoreFields:List[String],
  var gpModels:List[GpSpecification],
  buildInBackground:Boolean,
  var currentVis:VisInfo,
  currentRegion:RegionSpecification,
  history:Option[HistorySpecification]
)

object Project {

  // Serializers to get the json parser to work
  implicit val formats = net.liftweb.json.DefaultFormats

  def recent : Array[ProjectInfo] = {
    Config.recentProjects flatMap {rp =>
      try {
        val json = loadJson(rp)
        Some(ProjectInfo(json.name, rp, new java.util.Date, 
                         json.inputs.length, json.outputs.length))
      } catch {
        case e:java.io.FileNotFoundException =>
          None
      }
    } toArray
  }

  def fromFile(path:String) : Project = {
    val config = loadJson(path)

    val sampleFilePath = Path.join(path, Config.sampleFilename)
    val samples = try {
      Table.fromCsv(sampleFilePath)
    } catch {
      case _:java.io.FileNotFoundException => new Table
    }

    val designSitePath = Path.join(path, Config.designFilename)
    val designSites = try {
      Table.fromCsv(designSitePath)
    } catch {
      case _:java.io.FileNotFoundException => new Table
    }

    // Figure out how many rows we've built the models on
    val gpDesignRows = config.gpModels.map(_.designMatrix.length) match {
      case Nil => 0
      case x   => x.min
    }

    val specifiedFields:List[String] = 
      config.inputs.map(_.name) ++ 
      config.outputs.map(_.name) ++ 
      config.ignoreFields

    val proj = if(samples.numRows > 0) {
      new RunningSamples(config, path, samples, designSites)
    } else if(gpDesignRows < designSites.numRows) {
      new BuildingGp(config, path, designSites)
    } else if(!designSites.fieldNames.diff(specifiedFields).isEmpty) {
      new NewResponses(config, path, designSites.fieldNames)
    } else {
      new Viewable(config, path, designSites)
    }

    proj
  }

  def mapInputs(inputs:List[(String,Float,Float)]) = 
    inputs.map {case (fld, mn, mx) =>
      InputSpecification(fld, mn, mx)
    }
  
  private def loadJson(path:String) : ProjConfig = {
    val configFilePath = Path.join(path, Config.projConfigFilename)
    val json = parse(Source.fromFile(configFilePath).mkString)
    try {
      json.extract[ProjConfig]
    } catch {
      case me:net.liftweb.json.MappingException =>
        throw new ProjectLoadException(me.msg, me)
    }
  }
}

case class ProjectInfo(name:String, path:String, 
                       modificationDate:Date, 
                       numInputs:Int, numOutputs:Int) {

  val statusString = "Ok"
}

sealed abstract class Project(config:ProjConfig) {
  
  // Serializers to get the json parser to work
  implicit val formats = net.liftweb.json.DefaultFormats
  
  def save(savePath:String) : Unit = {
    // Ensure that the project directory exists
    var pathDir = new File(savePath).mkdir

    val jsonPath = Path.join(savePath, Config.projConfigFilename)
    val outFile = new FileWriter(jsonPath)
    outFile.write(pretty(render(decompose(config))))
    outFile.close
    
    // Try to save the sample tables
    this match {
      case s:Sampler => s.saveSampleTables(savePath)
      case _         => 
    }
  }

  /**
   * The next stage in the project's evolution
   */
  def next : Project

  val name = config.name

  val scriptPath = config.scriptPath

  val inputs = new DimRanges(config.inputs.map {x =>
    (x.name -> (x.minRange, x.maxRange))
  } toMap)

  val modificationDate:Date

  def statusString:String

  def responses = config.outputs.map {x => (x.name, x.minimize)}

  def ignoreFields = config.ignoreFields.sorted

  def inputFields = inputs.dimNames.sorted
  def responseFields = responses.map(_._1).sorted

}

abstract class InProgress(config:ProjConfig) 
    extends Project(config) with Actor {

  var buildInBackground:Boolean

  //def start:Unit
  def stop:Unit

  private var eventListeners:ArrayBuffer[Actor] = ArrayBuffer()
  def addListener(a:Actor) = eventListeners.append(a)
  protected def publish(o:Any) = eventListeners.foreach {a => a ! o}
}

class NewProject(name:String, 
                 basePath:String,
                 scriptPath:String, 
                 inputDims:List[(String,Float,Float)]) 
    extends Project(ProjConfig(name, scriptPath, 
                               Project.mapInputs(inputDims),
                               Nil, Nil, Nil, false,
                               ViewInfo.Default,
                               Region.Default,
                               None)) with Sampler {

  val path = Path.join(basePath, name)

  val modificationDate = new Date

  val newSamples = new Table
  val designSites = new Table

  override def save(savePath:String) = {
    super.save(savePath)

    val sampleName = Path.join(savePath, Config.sampleFilename)
    newSamples.toCsv(sampleName)
  }

  def statusString = "New"

  def sampleRanges = 
    new DimRanges(inputDims.map(x => (x._1, (x._2, x._3))).toMap)
  
  def next = {
    save(path)
    //Project.fromFile(path).asInstanceOf[RunningSamples]
    Project.fromFile(path)
  }
}

class RunningSamples(config:ProjConfig, val path:String, 
                     val newSamples:Table, val designSites:Table) 
    extends InProgress(config) with Saved {
  
  var buildInBackground:Boolean = config.buildInBackground

  // See if we should start running some samples
  var sampleRunner:Option[SampleRunner] = None 
  if(buildInBackground) start

  def statusString = 
    "Running Samples (%s/%s)".format(currentTime.toString, totalTime.toString)
  
  val totalTime = newSamples.numRows
  var currentTime = 0

  override def save(savePath:String) = {
    super.save(savePath)

    // Also save the samples
    val sampleName = Path.join(savePath, Config.sampleFilename)
    newSamples.toCsv(sampleName)

    // Also save the design points
    val designName = Path.join(savePath, Config.designFilename)
    designSites.toCsv(designName)
  }

  override def start = {
    runSamples
    super.start
  }
  def stop = {}

  def next = {
    save()
    Project.fromFile(path).asInstanceOf[BuildingGp]
  }

  def act = {
    // Publish one of these right at the beginning just to get things going
    publish(Progress(currentTime, totalTime, statusString, true))

    var finished = false
    loopWhile(!finished) {
      react {
        case x:ConsoleLine => 
          publish(x)
        case SamplesCompleted(num) => 
          currentTime += num
          publish(Progress(currentTime, totalTime, statusString, true))
        case SamplingError(exitCode) =>
          val msg = "Sampler script exited with code: " + exitCode
          publish(Progress(currentTime, totalTime, msg, false))
        case SamplingComplete =>
          finished = true
          publish(ProgressComplete)
      }
    }
  }

  private def runSamples = {
    // only run if we aren't running something
    if(!sampleRunner.isDefined && newSamples.numRows > 0) {
      val runner = new SampleRunner(this)
      runner.start
      sampleRunner = Some(runner)
    }
  }
}

class BuildingGp(config:ProjConfig, val path:String, designSites:Table) 
    extends InProgress(config) with Saved {
  
  var buildInBackground:Boolean = config.buildInBackground
  
  //val gps = responseFields.map(fld => (fld, loadGpModel(gp, fld))).toMap

  def statusString = "Building GP"

  def stop = {}

  def act = {
    publish(Progress(-1, -1, statusString, true))
    // Build the gp models
    val designSiteFile = Path.join(path, Config.designFilename)
    val gp = new Rgp(designSiteFile)

    val buildFields = designSites.fieldNames.diff(inputFields++ignoreFields)

    val newModels = buildFields.map({fld => 
      println("building model for " + fld)
      (fld, gp.buildModel(inputFields, fld, Config.errorField))
    }).toMap
    config.gpModels = newModels.values.map(_.toJson).toList
    save()
    publish(ProgressComplete)
  }

  def next = {
    save()
    Project.fromFile(path)
  }

  override def save(savePath:String) = {
    super.save(savePath)

    // create an empty model samples table
    val fakeTable = new Table
    val filepath = Path.join(savePath, Config.respSampleFilename)
    fakeTable.toCsv(filepath)
  }

}

class NewResponses(config:ProjConfig, val path:String, allFields:List[String])
    extends Project(config) with Saved {
  
  def statusString = "New Responses"

  def addResponse(field:String, minimize:Boolean) = {
    if(!responseFields.contains(field)) {
      config.outputs = OutputSpecification(field, minimize) :: config.outputs
    }
  }

  def addIgnore(field:String) = {
    if(!ignoreFields.contains(field)) {
      config.ignoreFields = field :: config.ignoreFields
    }
  }

  def newFields : List[String] = {
    val knownFields : Set[String] = 
      (responseFields ++ ignoreFields ++ inputFields).toSet
    allFields.filter {fn => !knownFields.contains(fn)}
  }

  def next = {
    save
    Project.fromFile(path).asInstanceOf[Viewable]
  }

}

class Viewable(config:ProjConfig, val path:String, val designSites:Table) 
    extends Project(config) with Saved with Sampler {

  import Project._

  val newSamples = new Table

  // The visual controls
  val viewInfo = ViewInfo.fromJson(this, config.currentVis)

  var _region:Region = Region.fromJson(config.currentRegion, this)

  val gpModels:SortedMap[String,GpModel] = SortedMap[String,GpModel]() ++
    config.gpModels.map {gpConfig =>
      (gpConfig.responseDim, GpModel.fromJson(gpConfig))
    }

  val history:HistoryManager = config.history match {
    case Some(hc) => HistoryManager.fromJson(hc)
    case None     => new HistoryManager
  }

  val candidateGenerator = new CandidateGenerator(this)

  val previewImages:Option[PreviewImages] = loadImages(path)

  // Also set up a table of samples from each gp model
  val modelSamples:Table = loadResponseSamples(path)

  save()

  override def save(savePath:String) : Unit = {
    // Update the view info
    config.currentVis = viewInfo.toJson

    super.save(savePath)

    // Save the model samples
    if(modelSamples.numRows > 0) {
      val filepath = Path.join(savePath, Config.respSampleFilename)
      modelSamples.toCsv(filepath)
    }
  }

  def next = {
    save()
    Project.fromFile(path).asInstanceOf[RunningSamples]
  }

  def statusString = "Ok"

  def sampleRanges = _region.toRange

  def region : Region = _region
  def region_=(r:Region) = {
    _region = r
  }

  def numSamplesInRegion = {
    var count = 0
    for(i <- 0 until designSites.numRows) {
      val tpl = designSites.tuple(i)
      val inputs = tpl.filterKeys {k => inputFields contains k}
      if(region.inside(inputs.toList))
        count += 1
    }
    count
  }

  /**
   * The number of sample points that are unclipped 
   * by the current zoom level
   */
  def numUnclippedPoints : Int = {
    val (active,_) = viewFilterDesignSites
    active.numRows
  }

  def newFields : List[String] = {
    val knownFields : Set[String] = 
      (responseFields ++ ignoreFields ++ inputFields).toSet
    designSites.fieldNames.filter {fn => !knownFields.contains(fn)}
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

    var (minDist, minRow) = (Double.MaxValue, designSites.tuple(0))
    for(r <- 0 until designSites.numRows) {
      val tpl = designSites.tuple(r)
      val dist = ptDist(tpl)
      if(dist < minDist) {
        minDist = dist
        minRow = tpl
      }
    }
    minRow.toList
  }

  def viewFilterDesignSites : (Table,Table) = {
    val active = new Table
    val inactive = new Table
    for(r <- 0 until designSites.numRows) {
      val tpl = designSites.tuple(r).toList
      if(viewInfo.inView(tpl)) active.addRow(tpl)
      else                     inactive.addRow(tpl)
    }
    (active, inactive)
  }

  def estimatePoint(point:List[(String,Float)]) 
        : Map[String,(Float,Float,Float)] = {
    gpModels.map {case (fld, model) =>
      val (est, err) = model.runSample(point)
      (fld -> (est.toFloat, err.toFloat, 
               model.calcExpectedGain(est, err).toFloat))
    }
  }

  def value(point:List[(String,Float)]) : Map[String,Float] = {
    estimatePoint(point) map {case (k,v) => (k -> v._1)}
  }

  def value(point:List[(String,Float)], response:String) : Float = {
    gpModels(response).runSample(point)._1.toFloat
  }

  /**
   * Use the std deviation for the model as the "official" uncertainty
   */
  def uncertainty(point:List[(String,Float)]) : Map[String,Float] = {
    estimatePoint(point) map {case (k,v) => (k -> v._2)}
  }

  /**
   * Use the std deviation for the model as the "official" uncertainty
   */
  def uncertainty(point:List[(String,Float)], response:String) : Float = {
    gpModels(response).runSample(point)._2.toFloat
  }

  def expectedGain(point:List[(String,Float)]) : Map[String,Float] = 
    estimatePoint(point) map {case (k,v) => 
      (k -> gpModels(k).calcExpectedGain(v._1, v._2).toFloat)
    }

  def expectedGain(point:List[(String,Float)], response:String) : Float = {
    val sample = gpModels(response).runSample(point)
    gpModels(response).calcExpectedGain(sample._1, sample._2).toFloat
  }

  def randomSample2dResponse(resp1Dim:(String,(Float,Float)), 
                             resp2Dim:(String,(Float,Float))) = {

    val numSamples = viewInfo.estimateSampleDensity * 2
    Density2D.density(modelSamples, numSamples, resp2Dim, resp1Dim)
  }

  def viewValueFunction : (List[(String,Float)],String)=>Float = 
    viewInfo.currentMetric match {
      case ViewInfo.ValueMetric => value
      case ViewInfo.ErrorMetric => uncertainty
      case ViewInfo.GainMetric  => expectedGain
    }

  def sampleMatrix(xDim:(String,(Float,Float)),
                   yDim:(String,(Float, Float)),
                   response:String,
                   point:List[(String,Float)]) : Matrix2D = {
    val remainingPt = point.filter {case (fld,_) => 
      fld!=xDim._1 && fld!=yDim._1
    }
    val outData = tuner.Sampler.regularSlice(xDim, yDim, viewInfo.estimateSampleDensity)

    // Populate the slice
    outData.rowIds.zipWithIndex.foreach {tmpx =>
      val (xval,x) = tmpx
      outData.colIds.zipWithIndex.foreach {tmpy =>
        val (yval,y) = tmpy
        val samplePt = (xDim._1,xval)::(yDim._1,yval)::remainingPt
        outData.set(x, y, viewValueFunction(samplePt, response))
      }
    }
    outData
  }

  private def loadResponseSamples(path:String) : Table = {
    // First try to load up an old file
    val samples = try {
      val filepath = Path.join(path, Config.respSampleFilename)
      val tmp = Table.fromCsv(filepath)
      if(tmp.numRows == 0) {
        tuner.Sampler.lhc(inputs, Config.numericSampleDensity)
      } else {
        tmp
      }
    } catch {
      case e:java.io.FileNotFoundException => 
        tuner.Sampler.lhc(inputs, Config.numericSampleDensity)
    }
    gpModels.foldLeft(samples) {case (tbl, (fld, model)) =>
      if(!tbl.fieldNames.contains(fld)) {
        println("sampling response " + fld)
        gpModels(fld).sampleTable(tbl)
      } else {
        tbl
      }
    }
  }

  private def loadImages(path:String) : Option[PreviewImages] = {
    if(!gpModels.isEmpty) {
      val model = gpModels.values.head
      val imagePath = Path.join(path, Config.imageDirname)
      try {
        Some(new PreviewImages(model, imagePath, designSites))
      } catch {
        case e:java.io.FileNotFoundException => 
          //e.printStackTrace
          println("Could not find images, disabling")
          None
      }
    } else {
      None
    }
  }

}

