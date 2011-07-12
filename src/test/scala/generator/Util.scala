package tuner.test.generator

import org.scalacheck._

object Util {
  
  def fieldListType(size:Int) : Gen[List[String]] = {
    Gen.listOfN(size, Arbitrary.arbitrary[String])
  }

  def rangedFieldType : Gen[(String,Float,Float)] = for {
    fld <- Arbitrary.arbitrary[String]
    min <- Arbitrary.arbitrary[Float]
    max <- Arbitrary.arbitrary[Float] suchThat (_ > min)
  } yield (fld, min, max)

  def rangedFieldsType(size:Int) : Gen[List[(String,Float,Float)]] = 
    Gen.listOfN(size, rangedFieldType)

  def pathType : Gen[String] = for {
    len <- Gen.choose(1, 5)
    paths <- Gen.listOfN(len, Arbitrary.arbitrary[String])
  } yield paths.map(_.replace(" ", "_")).reduceLeft(_+"/"+_) + ".sh"
}

