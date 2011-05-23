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

class HistoryManager {
  
  var _points:Map[String,List[(String,Float)]] = Map()

  def add(name:String, position:List[(String,Float)]) : Unit = {
    _points += (name -> position)
  }

  def add(position:List[(String,Float)]) : Unit = {
    // First make sure this point isn't stored yet
    if(!_points.values.exists(x => x == position)) {
      add("History " + _points.size, position)
    }
  }

  def rename(oldName:String, newName:String) = {
    val pt = _points(oldName)
    _points -= oldName
    _points += (newName -> pt)
  }

  def point(name:String) = _points(name)

  def names : List[String] = _points.keys.toList.sorted

  def size = _points.size

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

