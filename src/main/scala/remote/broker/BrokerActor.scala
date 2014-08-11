package remote.broker

import akka.actor.Actor
import akka.actor.ActorLogging

class BrokerActor extends Actor with ActorLogging {
  def receive = {
    case msg : String =>
      val me = self.path.toString
      log.info(s"${me} got ${msg}")
      val remote = sender.path.toString
      sender() ! s"${remote}>${msg}"
  }
}
