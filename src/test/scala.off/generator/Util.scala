package tuner.test.generator

import org.scalacheck._
import org.scalatest.FunSuite
import org.scalatest.prop.Checkers

object Util {
  
  def fieldNameType : Gen[String] = for {
    n <- Gen.choose(1,50)
    str <- Gen.listOfN(n, Gen.oneOf(Gen.choose('a','z'),
                                    Gen.choose('A', 'Z')))
  } yield str.mkString

  def fieldListType(size:Int) : Gen[List[String]] = {
    Gen.listOfN(size, fieldNameType) suchThat (_.toSet.size == size)
  }

  def rangedFieldType : Gen[(String,Float,Float)] = for {
    fld <- fieldNameType
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

class UtilSpec extends FunSuite with Checkers {

  val fieldGen = Gen.sized {n => Util.fieldListType(n)}

  test("fields should not contain commas") {
    check(Prop.forAll(Util.fieldNameType) {field =>
      (!field.contains(","))
    })
  }

  test("field lists should have no commas") {
    check(Prop.forAll(fieldGen suchThat (_.length>0)) {fields =>
      (!fields.forall(_.contains(",")))
    })
  }

  test("field lists should be unique") {
    check(Prop.forAll(fieldGen suchThat (_.length>0)) {fields =>
      (fields.toSet.size == fields.length)
    })
  }

}

