package tuner.project

import net.liftweb.json.Extraction._
import net.liftweb.json.JsonParser._
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._

import java.io.File
import java.io.FileWriter
import java.util.Date

import scala.actors.Actor.actor
import scala.collection.immutable.SortedMap
import scala.io.Source

import tuner.CandidateGenerator
import tuner.Config
import tuner.DimRanges
import tuner.GpModel
import tuner.GpSpecification
import tuner.HistoryManager
import tuner.HistorySpecification
import tuner.PreviewImages
import tuner.Region
import tuner.RegionSpecification
import tuner.Rgp
import tuner.SampleRunner
import tuner.Table
import tuner.ViewInfo
import tuner.VisInfo
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
  gpModels:List[GpSpecification],
  buildInBackground:Boolean,
  currentVis:VisInfo,
  currentRegion:RegionSpecification,
  history:Option[HistorySpecification]
)

object Project {

  // Serializers to get the json parser to work
  implicit val formats = net.liftweb.json.DefaultFormats

  def recent : Array[Project] = {
    Config.recentProjects flatMap {rp =>
      try {
        Some(Project.fromFile(rp))
      } catch {
        case e:java.io.FileNotFoundException =>
          None
      }
    } toArray
  }

  def fromFile(path:String) = {
    val configFilePath = Path.join(path, Config.projConfigFilename)
    val json = parse(Source.fromFile(configFilePath).mkString)
    val config = json.extract[ProjConfig]

    val sampleFilePath = path + "/" + Config.sampleFilename
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

    val specifiedFields = config.inputs.length + 
                          config.outputs.length + 
                          config.ignoreFields.length

    val proj = if(samples.numRows > 0) {
      new RunningSamples(config, path, samples)
    } else if(config.gpModels.length < config.outputs.length) {
      new BuildingGp(config, path, designSites)
    } else if(designSites.fieldNames.length > specifiedFields) {
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

}

sealed abstract class Project(config:ProjConfig) {
  
  // Serializers to get the json parser to work
  implicit val formats = net.liftweb.json.DefaultFormats
  
  def save(savePath:String) : Unit = {
    val saveFile = savePath + "/" + Config.projConfigFilename
    val outFile = new FileWriter(saveFile)
    outFile.write(pretty(render(decompose(config))))
    outFile.close
  }

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

class NewProject(name:String, 
                 val savePath:String,
                 scriptPath:String, 
                 inputDims:List[(String,Float,Float)]) 
    extends Project(ProjConfig(name, scriptPath, 
                               Project.mapInputs(inputDims),
                               Nil, Nil, Nil, false,
                               ViewInfo.DefaultVisInfo,
                               Region.DefaultRegionInfo,
                               None)) with Sampler {

  val modificationDate = new Date

  val newSamples = new Table
  val designSites = new Table

  def statusString = "New"

  def sampleRanges = 
    new DimRanges(inputDims.map(x => (x._1, (x._2, x._3))).toMap)
}

class BuildingGp(config:ProjConfig, val path:String, designSites:Table) 
    extends Project(config) with Saved with InProgress {
  
  var gpBuilt = false
  
  // Build the gp models
  val designSitesPath = path + "/" + Config.designFilename
  val gp = new Rgp(designSitesPath)
  //val gps = responseFields.map(fld => (fld, loadGpModel(gp, fld))).toMap

  gpBuilt = true

  def statusString = "Building GP"

  def currentTime = -1
  def totalTime = -1

}

class RunningSamples(config:ProjConfig, val path:String, val newSamples:Table) 
    extends Project(config) with Saved with InProgress {
  

  def statusString = "Running Samples"

  def currentTime = 10
  def totalTime = 100

  val designSites = new Table

  // See if we should start running some samples
  var sampleRunner:Option[SampleRunner] = None 
  if(buildInBackground) runSamples

  private def runSamples = {
    // only run if we aren't running something
    if(!sampleRunner.isDefined && newSamples.numRows > 0) {
      val runner = new SampleRunner(this)
      runner.start
      sampleRunner = Some(runner)
    }
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

  def statusString = "Ok"
  val previewImages:Option[PreviewImages] = loadImages(path)

  // Also set up a table of samples from each gp model
  lazy val modelSamples:Table = loadResponseSamples(path)

  // Save any gp models that got updated
  //save(savePath)
  save
  //path.foreach(_ => save(savePath))

  /*
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
    } else if(!gpModelsReady) {
      Project.BuildingGp
    } else {
      Project.Ok
    }
  }
  */

  /*
  def inputs : DimRanges = _inputs
  def inputs_=(dr:DimRanges) = {
    _inputs = dr
    region = Region(Region.Box, this)
    _inputs.dimNames.foreach {fld =>
      region.setRadius(fld, 0f)
    }
  }
  */

  def region : Region = _region
  def region_=(r:Region) = {
    _region = r
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

  def estimatePoint(point:List[(String,Float)]) 
        : Map[String,(Float,Float,Float)] = {
    gpModels.map {case (fld, model) =>
      val (est, err) = model.runSample(point)
      (fld -> (est.toFloat, err.toFloat, 
               model.calcExpectedGain(est.toFloat, err.toFloat)))
    }
  }

  def randomSample2dResponse(resp1Dim:(String,(Float,Float)), 
                             resp2Dim:(String,(Float,Float))) = {

    val numSamples = viewInfo.estimateSampleDensity * 2
    Density2D.density(modelSamples, numSamples, resp2Dim, resp1Dim)
  }

  private def loadResponseSamples(path:String) : Table = {
    // First try to load up an old file
    val samples = try {
      val filepath = Path.join(path, Config.respSampleFilename)
      Table.fromCsv(filepath)
    } catch {
      case e:java.io.FileNotFoundException => 
        tuner.Sampler.lhc(inputs, Config.respHistogramSampleDensity)
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

