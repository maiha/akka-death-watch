package remote.edge

import akka.actor.Actor

class EdgeActor(val remote: String) extends Actor with Retry {
  import Service._
}
