package tuner

import scala.swing.event.Event

case class Progress(currentTime:Int, totalTime:Int, msg:String, ok:Boolean) extends Event
case object ProgressComplete extends Event
case class ConsoleLine(line:String) extends Event
case class SamplesCompleted(num:Int) extends Event
case object SamplingComplete extends Event

