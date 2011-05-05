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
    val p = new Project(Some(path))

    // Only add this project if the loading worked out
    Config.recentProjects += path

    p
  }

  object Status extends Enumeration {
    val Ok = Value("Ok")
  }
}

class Project(val path:Option[String]) {

  def this() = this(None)

  // Serializers to get the json parser to work
  implicit val formats = net.liftweb.json.DefaultFormats

  var config = path map {p =>
    val projConfigPath = p + "/" + Config.projConfigFilename
    val json = parse(Source.fromFile(projConfigPath).toString)
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
  }

  val samples = if(path.isDefined) {
    val sampleFilename = path + "/" + Config.sampleFilename
    Table.fromCsv(sampleFilename)
  } else {
    new Table
  }

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

  def save(path:String) = {
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
    var pathDir = new File(path).mkdir

    val jsonPath = path + "/" + Config.projConfigFilename
    val outFile = new FileWriter(jsonPath)
    outFile.write(pretty(render(json)))
    outFile.close

    // Also save the samples
    val sampleName = path + "/" + Config.sampleFilename
    samples.toCsv(sampleName)
  }

}

