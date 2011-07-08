package tuner.test.generator

import org.scalacheck._

import tuner.Table

object TableGen {
  
  def tableType(rows:Int) : Gen[Table] = for {
    d <- Gen.choose(1, 10)
    fields <- Util.fieldListType(d)
    vals <- Gen.listOfN(rows, Gen.listOfN(d, Arbitrary.arbitrary[Float]))
  } yield {
    val tbl = new Table
    vals.foreach {row =>
      tbl.addRow(fields.zip(row))
    }
    tbl
  }
}

