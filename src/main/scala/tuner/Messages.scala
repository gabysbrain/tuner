package tuner

case class Progress(currentTime:Int, totalTime:Int, msg:String, ok:Boolean)
case object ProgressComplete
case class ConsoleLine(line:String)
case class SamplesCompleted(num:Int)
case class SamplingError(exitCode:Int)
case object SamplingComplete

