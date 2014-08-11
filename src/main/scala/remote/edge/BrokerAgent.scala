package remote.edge

import akka.actor.Actor
import util.Retry

class BrokerAgent(val remote: String) extends Actor with Retry {
}
