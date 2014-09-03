package tuner.test.unit

import tuner.Table
import tuner.gui.util.Histogram

class HistogramSpec extends UnitTest {
  val t1 = {
    val tblData = List(
      List(("x1", 2.0f), ("x2", 0.2f), ("x3", 1.2f))
    )
    val tbl = new Table
    tblData.foreach {row => tbl.addRow(row)}
    tbl
  }
  val t2 = {
    val tblData = List(
      List(("lambda1", 2.0f), ("lambda2", 0.2f), ("lambda3", 1.2f)),
      List(("lambda1", 2.1f), ("lambda2", 0.4f), ("lambda3", 0.3f)),
      List(("lambda1", 0.1f), ("lambda2", 0.9f), ("lambda3", 0.5f))
    )
    val tbl = new Table
    tblData.foreach {row => tbl.addRow(row)}
    tbl
  }
  
  "A Histogram" should {
    "throw an exception" should {
      "given a table with a single row" in {
        Histogram.countData("x1", t1, 3) must throwA[IllegalArgumentException]
      }
    }

    "return a histogram with n+1 buckets" should {
      "given a valid input table" in {
        val m = Histogram.countData("lambda1", t2, 3)
        m must have size (4)
      }
    }
  }
}

