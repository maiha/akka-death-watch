package remote.broker

import akka.actor.Actor

class BrokerActor extends Actor {
  def receive = {
    case "PING" =>
      debug("recv: PING")
      sender() ! "PONG"
    case msg : String =>
      debug(s"got [${msg}]")
      sender() ! s"[String: ${msg}]"
    case msg : Any =>
      debug(s"got unknown [${msg}]")
      sender() ! s"[???: ${msg}]"
  }

  private def debug(msg: String) {
    println(msg)
  }
}
