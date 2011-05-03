package scala.swing

import info.clearthought.layout.TableLayout
import info.clearthought.layout.TableLayoutConstants
import info.clearthought.layout.TableLayoutConstraints

object TablePanel {
  object Size {
    val Fill = TableLayoutConstants.FILL
    val Preferred = TableLayoutConstants.PREFERRED
    val Minimum = TableLayoutConstants.MINIMUM
  }

  object HorizAlign extends Enumeration {
    val Left = Value(TableLayoutConstants.LEFT)
    val Center = Value(TableLayoutConstants.CENTER)
    val Full = Value(TableLayoutConstants.FULL)
    val Right = Value(TableLayoutConstants.RIGHT)
    val Leading = Value(TableLayoutConstants.LEADING)
    val Trailing = Value(TableLayoutConstants.TRAILING)
  }

  object VertAlign extends Enumeration {
    val Top = Value(TableLayoutConstants.TOP)
    val Center = Value(TableLayoutConstants.CENTER)
    val Bottom = Value(TableLayoutConstants.BOTTOM)
  }
}

class TablePanel(colSz0:List[Double],rowSz0:List[Double]) extends Panel 
                                                          with LayoutContainer {
  import TablePanel._

  override lazy val peer = 
    new javax.swing.JPanel(
      new TableLayout(colSz0.toArray,rowSz0.toArray)) 
      with SuperMixin

  private def layoutManager = peer.getLayout.asInstanceOf[TableLayout]

  // Let user specify layout with just a column,row pair
  implicit def pair2Constraints(p:(Int,Int)) : Constraints = {
    val c = new Constraints
    c.ulCol = p._1
    c.ulRow = p._2
    c
  }

  implicit def cellSpec2Constraints(p:(Int,Int,HorizAlign.Value,VertAlign.Value)) : Constraints = {
    val c = new Constraints
    c.ulCol = p._1
    c.ulRow = p._2
    c.hAlign = p._3
    c.vAlign = p._4
    c
  }

  class Constraints(val peer:TableLayoutConstraints) extends Proxy {
    def self = peer

    def this() = this(new TableLayoutConstraints)

    def ulCol = peer.col1
    def ulCol_=(c:Int) {peer.col1 = c}

    def ulRow = peer.row1
    def ulRow_=(r:Int) {peer.row1 = r}

    def lrCol = peer.col2
    def lrCol_=(c:Int) {peer.col2 = c}

    def lrRow = peer.row2
    def lrRow_=(r:Int) {peer.row2 = r}

    def hAlign : HorizAlign.Value = HorizAlign(peer.hAlign)
    def hAlign_=(a:HorizAlign.Value) {peer.hAlign = a.id}

    def vAlign : VertAlign.Value = VertAlign(peer.vAlign)
    def vAlign_=(a:VertAlign.Value) {peer.vAlign = a.id}
  }

  protected def constraintsFor(comp:Component) =
    new Constraints(layoutManager.getConstraints(comp.peer))

  protected def areValid(c:Constraints) : (Boolean,String) = (true,"")
  protected def add(c:Component, l:Constraints) {peer.add(c.peer, l.peer)}
}
