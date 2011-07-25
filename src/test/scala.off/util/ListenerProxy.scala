package tuner.test.util

import scala.actors.Actor._

case object SignalFound

object ListenerProxy {
  
  def waitFor(publisher:tuner.project.InProgress, msec:Long)
             (event:PartialFunction[Any,Any]) : Option[Any] = {
    val a = actor {
      val result = receiveWithin(msec)(event)
      receive {
        case SignalFound => reply(result)
      }
    }
    publisher.addListener(a)
    Some(a !? SignalFound)
  }
}

