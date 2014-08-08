package remote.broker

import akka.actor.Actor

class BrokerActor extends Actor {
  def receive = {
    case "PING" =>
      sender() ! "PONG"
    case msg : String =>
      debugLog(s"broker got String: ${msg}")
      sender() ! s"[String: ${msg}]"
    case msg : Any =>
      debugLog(s"broker got unknown msg: ${msg}")
      sender() ! s"[???: ${msg}]"
  }

  private def debugLog(msg: String) {
    println(s"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
    println(msg)
    println(s"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
  }
}
