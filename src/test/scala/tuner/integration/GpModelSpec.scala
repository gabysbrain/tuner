package tuner.test.integration

import net.liftweb.json._
import net.liftweb.json.Extraction._
import scala.io.Source
import scala.util.Try

import tuner.gp.GpModel
import tuner.gp.GpSpecification

import tuner.test.Util._

class GpModelSpec extends IntegrationTest {

  implicit val formats = net.liftweb.json.DefaultFormats

  "A GpModel" should {
    "given a valid 8D json file to load" should {
      val jsonPath = resource("/gp/8d_1.json")
      val json = parse(Source.fromFile(jsonPath).mkString)

      "load the model without errors" in {
        val proj = json.extract[GpSpecification]
        Try(GpModel.fromJson(proj)) must beSuccessfulTry
      }
    }

    "given a valid 3D json file to load" should {
      val jsonPath = resource("/gp/3d_1.json")
      val json = parse(Source.fromFile(jsonPath).mkString)

      "load the model without errors" in {
        val proj = json.extract[GpSpecification]
        Try(GpModel.fromJson(proj)) must beSuccessfulTry
      }

      "create json that is reloadable" in {
        val gp = GpModel.fromJson(json.extract[GpSpecification])
        val gpJson = gp.toJson
        GpModel.fromJson(json.extract[GpSpecification]) must_== gp
      }
    }

    "2 models are loaded from the same dataset they" should {
      val jsonPath = resource("/gp/3d_1.json")
      val json = parse(Source.fromFile(jsonPath).mkString)

      "equal each other" in {
        val gp1 = GpModel.fromJson(json.extract[GpSpecification])
        val gp2 = GpModel.fromJson(json.extract[GpSpecification])
        gp1 must_== gp2
      }
    }

    "given another valid 3D json file to load" should {
      val jsonPath = resource("/gp/3d_2.json")
      val json = parse(Source.fromFile(jsonPath).mkString)

      "load the model without errors" in {
        val proj = json.extract[GpSpecification]
        Try(GpModel.fromJson(proj)) must beSuccessfulTry
      }
    }
  }

}

