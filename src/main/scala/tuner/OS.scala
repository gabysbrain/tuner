package tuner

import scala.util.Properties

object OS {
  sealed trait OS
  case object Mac extends OS
  case object Win extends OS

  def detect : OS = {
    if(isMac) Mac
    else      Win
  }
  
  def isMac = Properties.isMac
  def isWin = Properties.isWin
}



