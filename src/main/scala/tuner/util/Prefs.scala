package tuner.util

import java.util.prefs.Preferences

import tuner.Tuner

/**
 * A simple wrapper class around java's preferences.  Has convenience
 * functions for storing things like lists
 */
object Prefs {
  val userPrefs = Preferences.userNodeForPackage(Tuner.getClass)

  def int(key:String, default:Int) = userPrefs.getInt(key, default)

  def list(key:String) : List[String] = {
    val parent = userPrefs.node(key)
    parent.keys map {k => parent.get(k, "")} toList
  }

  def saveList(key:String, v:List[String]) = {
    val parent = userPrefs.node(key)
    v.zipWithIndex foreach {case (vv,i) =>
      parent.put("Item-" + i, vv)
    }
  }
}

