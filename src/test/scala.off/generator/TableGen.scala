package tuner.test.generator

import org.scalacheck._

import tuner.Table

object TableGen {
  
  def tableType(rows:Int, fields:List[String]) = for {
    vals <- Gen.listOfN(rows, Gen.listOfN(fields.length, Gen.choose(0f,1f)))
  } yield {
    val tbl = new Table
    vals.foreach {row =>
      tbl.addRow(fields.zip(row))
    }
    tbl
  }

  def tableType(size:Int) : Gen[Table] = for {
    d <- Gen.choose(3, 10)
    fields <- Util.fieldListType(d)
    tbl <- tableType(size, fields)
  } yield {
    tbl
  }

  def wideTableType(size:Int) : Gen[Table] = for {
    d <- Gen.choose(size, size*2)
    fields <- Util.fieldListType(d)
    tbl <- tableType(size, fields)
  } yield {
    tbl
  }

  def trainTestTableGen : Gen[(Table,Table)] = Gen.sized {size => for {
    trainTbl <- tableType(size)
    testTbl <- tableType(size, trainTbl.fieldNames)
  } yield {
    (trainTbl, testTbl)
  }}

  val tableGen = Gen.sized {size => tableType(size)}
  val wideTableGen = Gen.sized {size => wideTableType(size)}
}

