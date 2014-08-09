package remote.edge

import akka.actor.Actor

class EdgeActor(val path: String) extends Actor with Retry {
  import Service._
}
