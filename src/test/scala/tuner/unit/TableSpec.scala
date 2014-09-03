package tuner.test.unit

import tuner.Table

class TableSpec extends UnitTest {
  val tbl = {
    val tblData = List(
      List(("lambda1", 2.0f), ("lambda2", 0.2f), ("lambda3", 1.2f)),
      List(("lambda1", 2.1f), ("lambda2", 0.2f), ("lambda3", 0.3f)),
      List(("lambda1", 0.1f), ("lambda2", 0.2f), ("lambda3", 0.5f))
    )
    val tbl = new Table
    tblData.foreach {row => tbl.addRow(row)}
    tbl
  }

  "A Table must return the same number of values as rows" should {
    "queried for a field with all different values" in {
      tbl.values("lambda1") must have size (tbl.numRows)
    }
    "queried for a field with all the same values" in {
      tbl.values("lambda2") must have size (tbl.numRows)
    }
  }

}

