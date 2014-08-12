package scala.swing

import info.clearthought.layout.TableLayout
import info.clearthought.layout.TableLayoutConstants
import info.clearthought.layout.TableLayoutConstraints

import scala.swing.event.UIElementResized

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

  /** 
   * Creates a table with the specified number of rows and columns equally
   * sized
   */
  def this(nCol:Int,nRow:Int) = this(List.fill(nCol)(TablePanel.Size.Fill),
                                     List.fill(nRow)(TablePanel.Size.Fill))

  /**
   * Default empty table layout
   */
  //def this() = this(0, 0)

  // Let user specify layout with just a column,row pair
  implicit def p2c(p:(Int,Int)) : Constraints = {
    val c = new Constraints
    c.ulCol = p._1
    c.ulRow = p._2
    c
  }

  implicit def fs2c(p:(Int,Int,HorizAlign.Value,VertAlign.Value)) : Constraints = {
    val c = new Constraints
    c.ulCol = p._1
    c.ulRow = p._2
    c.hAlign = p._3
    c.vAlign = p._4
    c
  }

  implicit def ph2c(p:(Int,Int,HorizAlign.Value)) : Constraints = {
    val c = new Constraints
    c.ulCol = p._1
    c.ulRow = p._2
    c.hAlign = p._3
    c
  }

  implicit def pv2c(p:(Int,Int,VertAlign.Value)) : Constraints = {
    val c = new Constraints
    c.ulCol = p._1
    c.ulRow = p._2
    c.vAlign = p._3
    c
  }

  class Constraints(val peer:TableLayoutConstraints) extends Proxy {
    def self = peer

    def this() = this(new TableLayoutConstraints)

    def ulCol = peer.col1
    def ulCol_=(c:Int) = {
      peer.col1 = c
      if(peer.col2 < c)
        peer.col2 = c
    }

    def ulRow = peer.row1
    def ulRow_=(r:Int) = {
      peer.row1 = r
      if(peer.row2 < r)
        peer.row2 = r
    }

    def lrCol = peer.col2
    def lrCol_=(c:Int) {peer.col2 = c}

    def lrRow = peer.row2
    def lrRow_=(r:Int) {peer.row2 = r}

    def hAlign : HorizAlign.Value = HorizAlign(peer.hAlign)
    def hAlign_=(a:HorizAlign.Value) {peer.hAlign = a.id}

    def vAlign : VertAlign.Value = VertAlign(peer.vAlign)
    def vAlign_=(a:VertAlign.Value) {peer.vAlign = a.id}
  }

  def cols : Int = layoutManager.getNumColumn
  def rows : Int = layoutManager.getNumRow

  def addRow(sz:Double) = {
    layoutManager.insertRow(rows, sz)
    //layoutManager.layoutContainer(peer)
    //peer.repaint()
    publish(new UIElementResized(this))
  }

  def dropRow(r:Int) = {
    println("dropping row " + r)
    layoutManager.deleteRow(r)
    //layoutManager.layoutContainer(peer)
    //peer.repaint()
    publish(new UIElementResized(this))
  }

  def remove(c:Component) = peer.remove(c.peer)

  protected def constraintsFor(comp:Component) =
    new Constraints(layoutManager.getConstraints(comp.peer))

  protected def areValid(c:Constraints) : (Boolean,String) = (true,"")
  protected def add(c:Component, l:Constraints) = {
    peer.add(c.peer, l.peer)
  }
}

