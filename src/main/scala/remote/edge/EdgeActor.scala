package remote.edge

import akka.actor.{ ActorSystem, Actor, ActorRef, Props }

class EdgeActor(val path: String) extends Actor with Reconnect {
  import Service._
}
