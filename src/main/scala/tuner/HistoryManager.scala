package tuner

import net.liftweb.json.JsonParser._
import net.liftweb.json.JsonDSL._

case class HistorySlice(name:String, value:Float)
case class HistoryItemSpecification(name:String, position:List[HistorySlice])
case class HistorySpecification(contents:List[HistoryItemSpecification])

object HistoryManager {

  def fromJson(json:HistorySpecification) = {
    val mgr = new HistoryManager
    json.contents.foreach {item =>
      val pt = item.position.map {slice => (slice.name, slice.value)}
      mgr.add(item.name, pt)
    }
    mgr
  }
}

class HistoryManager extends NamedPointManager("History") {
  
  def toJson = {
    ("contents" -> _points.map {case (name, pt) =>
      ("name" -> name) ~
      ("position" -> pt.map {case (fld,v) =>
        ("name" -> fld) ~
        ("value" -> v)
      })
    }.toList)
  }
}

