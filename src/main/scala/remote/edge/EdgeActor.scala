package remote.edge

import akka.actor.Actor
import util.Retry

class EdgeActor(val remote: String) extends Actor with Retry {
}
