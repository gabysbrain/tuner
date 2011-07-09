package tuner.test.generator

import org.scalacheck._

import tuner.project.NewProject

object ProjectGen {

  def newProjectType : Gen[NewProject] = for {
    d <- Gen.choose(1, 20)
    inFields <- Util.rangedFieldsType(d)
    name <- Arbitrary.arbitrary[String]
    scriptPath <- Util.pathType
  } yield new NewProject(name, scriptPath, inFields)

}

