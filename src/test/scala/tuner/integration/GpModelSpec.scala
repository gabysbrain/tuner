package tuner.test.integration

import org.scalatest._
import org.scalatest.Matchers._

import net.liftweb.json._
import net.liftweb.json.Extraction._
import scala.io.Source

import tuner.gp.GpModel
import tuner.gp.GpSpecification

import tuner.test.Util._

class GpModelSpec extends WordSpec {

  implicit val formats = net.liftweb.json.DefaultFormats

  "A GpModel" when {
    "given a valid 8D json file to load" must {
      val jsonPath = resource("/gp/8d_1.json")
      val json = parse(Source.fromFile(jsonPath).mkString)

      "load the model without errors" in {
        val proj = json.extract[GpSpecification]
      }
    }

    "given a valid 3D json file to load" must {
      val jsonPath = resource("/gp/3d_1.json")
      val json = parse(Source.fromFile(jsonPath).mkString)

      "load the model without errors" in {
        val proj = json.extract[GpSpecification]
      }
    }

    "given another valid 3D json file to load" must {
      val jsonPath = resource("/gp/3d_2.json")
      val json = parse(Source.fromFile(jsonPath).mkString)

      "load the model without errors" in {
        val proj = json.extract[GpSpecification]
      }
    }
  }

}

