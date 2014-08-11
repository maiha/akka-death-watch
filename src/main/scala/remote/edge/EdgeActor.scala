package remote.edge

import akka.actor.Actor
import akka.actor.ActorRef

class EdgeActor(broker: ActorRef) extends Actor {
  def receive: Receive = {
    case msg =>
      broker.forward(msg)
  }
}
