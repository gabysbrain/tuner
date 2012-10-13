package tuner.test.generator

import org.scalacheck._

import tuner.Table

object TableGen {
  
  def tableType(size:Int) : Gen[Table] = for {
    d <- Gen.choose(3, 10)
    fields <- Util.fieldListType(d)
    vals <- Gen.listOfN(size, Gen.listOfN(d, Gen.choose(0f,1f)))
  } yield {
    val tbl = new Table
    vals.foreach {row =>
      tbl.addRow(fields.zip(row))
    }
    tbl
  }}
}

