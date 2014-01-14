package tuner.test.unit

import org.scalatest._
import org.scalatest.Matchers._

import tuner.Table
import tuner.gui.util.Histogram

class HistogramSpec extends WordSpec {
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
  
  "A Histogram" must {
    "throw an exception" when {
      "given a table with a single row" in {
        an [IllegalArgumentException] should be thrownBy Histogram.countData("x1", t1, 3)
      }
    }

    "return a histogram with n+1 buckets" when {
      "given a valid input table" in {
        val m = Histogram.countData("lambda1", t2, 3)
        m should have size (4)
      }
    }
  }
}

