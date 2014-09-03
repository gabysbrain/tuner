package tuner.test.unit

import tuner.Table
import tuner.error.InvalidSamplingTableException

class InvalidSamplingTableExceptionSpec extends UnitTest {
  
  val t1 = {
    val tblData = List(
      List(("x1", 2.0f), ("x2", 0.2f), ("x3", 1.2f)),
      List(("x1", 2.1f), ("x2", 0.4f), ("x3", 0.3f)),
      List(("x1", 0.1f), ("x2", 0.9f), ("x3", 0.5f))
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

  "An InvalidSamplingTableException" should {
    "given non-matching tables" should {
      "return a useful error message" in {
        val e = new InvalidSamplingTableException(t1, t2)
        e.getMessage must_!= null
      }
    }
  }

}

