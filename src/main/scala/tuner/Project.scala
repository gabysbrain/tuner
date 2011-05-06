package tuner

import net.liftweb.json.JsonParser._
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._

import java.io.File
import java.io.FileWriter
import java.util.Date

import scala.io.Source

// Internal config for matching with the json stuff
case class InputSpecification(name:String, minRange:Float, maxRange:Float)
case class ProjConfig(
  name:String,
  scriptPath:String,
  inputs:List[InputSpecification]
)

object Project {
  def recent : List[Project] = {
    Config.recentProjects map {Project.fromFile(_)} toList
  }

  def fromFile(path:String) = {
    new Project(Some(path))
  }

  object Status extends Enumeration {
    val Ok = Value("Ok")
  }
}

class Project(var path:Option[String]) {

  def this() = this(None)

  // Serializers to get the json parser to work
  implicit val formats = net.liftweb.json.DefaultFormats

  var config = path map {p =>
    val projConfigPath = p + "/" + Config.projConfigFilename
    val json = parse(Source.fromFile(projConfigPath).mkString)
    json.extract[ProjConfig]
  }

  var name : Option[String] = config map {_.name}
  var scriptPath : Option[String] = config map {_.scriptPath}
  def modificationDate : Date = new Date
  var status : Project.Status.Value = Project.Status.Ok
  var inputs : Option[DimRanges] = config.map {x =>
    new DimRanges(x.inputs map {x => 
      (x.name, (x.minRange, x.maxRange))
    } toMap)
    new DimRanges(Nil.toMap)
  }

  val samples = path.map({p =>
    val sampleFilename = p + "/" + Config.sampleFilename
    Table.fromCsv(sampleFilename)
  }).getOrElse(new Table)

  def addSamples(n:Int) = {
    // TODO: find a better solution than just ignoring the missing inputs
    inputs.map {i => 
      Sampler.regularGrid(i, n, {v => samples.addRow(v)})
      println(n + " samples generated")
    }
  }

  /**
   * Clears out the sample table then adds the samples
   */
  def newSamples(n:Int) = {
    samples.clear
    addSamples(n)
  }

  def save(savePath:String) = {
    path = Some(savePath)

    val json = (
      ("name" -> name) ~
      ("scriptPath" -> scriptPath) ~
      ("inputs" -> inputs.map {i => i.dimNames.map {dn =>
        ("name" -> dn) ~
        ("minRange" -> i.min(dn)) ~
        ("maxRange" -> i.max(dn))}}
      )
    )

    // Ensure that the project directory exists
    var pathDir = new File(savePath).mkdir

    val jsonPath = savePath + "/" + Config.projConfigFilename
    val outFile = new FileWriter(jsonPath)
    outFile.write(pretty(render(json)))
    outFile.close

    // Also save the samples
    val sampleName = savePath + "/" + Config.sampleFilename
    samples.toCsv(sampleName)
  }

}

