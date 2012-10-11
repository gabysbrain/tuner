package tuner.test.generator

import org.scalacheck._

import tuner.Table

object TableGen {
  
  def tableType : Gen[Table] = Gen.sized {rows => for {
    d <- Gen.choose(2, 10)
    fields <- Util.fieldListType(d)
    vals <- Gen.listOfN(rows, Gen.listOfN(d, Gen.choose(0f,1f)))
  } yield {
    val tbl = new Table
    vals.foreach {row =>
      tbl.addRow(fields.zip(row))
    }
    tbl
  }}
}

