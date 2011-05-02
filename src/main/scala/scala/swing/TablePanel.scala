package scala.swing

import info.clearthought.layout.TableLayout
import info.clearthought.layout.TableLayoutConstants
import info.clearthought.layout.TableLayoutConstraints

object TablePanel {
  object Size {
    val Fill = TableLayoutConstants.FILL
  }
}

class TablePanel(colSz0:List[Double],rowSz0:List[Double]) extends Panel with SequentialContainer.Wrapper {

  override lazy val peer = 
    new javax.swing.JPanel(new TableLayout(colSz0.toArray,rowSz0.toArray)) with SuperMixin

  def this(ncols:Int, nrows:Int) = 
    this(List.fill(nrows)(1.0/nrows), List.fill(ncols)(1.0/ncols))

  private def layoutManager = peer.getLayout.asInstanceOf[TableLayout]

}

