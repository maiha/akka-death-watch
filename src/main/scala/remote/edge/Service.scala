package remote.edge

import akka.actor.{ ActorSystem, Actor, ActorRef, Props }
import com.typesafe.config.ConfigFactory
import scala.concurrent.{ Future, Await }
import scala.concurrent.duration._

object Service {
  import util.RemoteAddress

  // args(0): local address
  // args(1): remote address
  def main(args: Array[String]) {
    if (args.size < 2)
      throw new RuntimeException("usage: local(addr) remote(addr)")

    val edge   = RemoteAddress.fromConfig("edge")
    val local  = RemoteAddress.parseWithDefault(args(0), edge).resolve

    val broker = RemoteAddress.fromConfig("broker")
    val remote = RemoteAddress.parseWithDefault(args(1), broker).resolve

    start(local, remote)
  }

  def start(local: RemoteAddress, remote: RemoteAddress) {
    val config = ConfigFactory.parseString(local.configString).withFallback(ConfigFactory.load("edge"))
    val system = ActorSystem("edge", config)
    val path   = remote.path("broker", "broker") // akka remote path for broker
    val agent  = system.actorOf(Props(classOf[BrokerAgent], path), "agent")
    val edge   = system.actorOf(Service.props(agent), "edge")

    //Use the system's dispatcher as ExecutionContext
    import system.dispatcher
    import akka.pattern.ask

    for (
      i <- 1 to 1000
    ) yield {
      val future: Future[Any] = edge.ask("hi")(10 seconds)
      future.map { res =>
        println(s"res: ${res}")
      }
      Thread.sleep(3000)
    }
  }

  def props(agent: ActorRef): Props = Props(new EdgeActor(agent))
}
