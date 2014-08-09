package remote.edge

import akka.actor.{ ActorSystem, Actor, ActorRef, Props }

// class EdgeActor(path: String) extends WorkerWatcher(path)

class EdgeActor(val path: String) extends Actor with Reconnect {
  import Service._
}
