package remote.broker

import akka.actor.Actor
import akka.actor.ActorLogging

class BrokerActor extends Actor with ActorLogging {
  val me = s"[${self.path}]"

  def receive = {
    case msg : String =>
      log.info(s"${me} got ${msg}")
      sender() ! s"${me}: ${msg}"
  }
}
