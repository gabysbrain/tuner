package tuner.project

import scala.actors.Actor._

trait InProgress {
  
  protected def totalTime : Int
  protected def currentTime : Int
  
  val statusActions:PartialFunction[Any,Unit] = {
    case CurrentRunStatus =>
      reply(CurrentRunStatusResponse(currentTime, totalTime))
  }
}

