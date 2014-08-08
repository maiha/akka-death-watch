package remote.edge

import akka.actor.{ ActorSystem, Actor, ActorRef, Props }
import com.typesafe.config.ConfigFactory
import scala.concurrent.{ Future, Await }
import scala.concurrent.duration._

object Service {
  def main(args: Array[String]) {
    args toList match {
      case port :: Nil =>
        lookup(port)
      case _ =>
        println("no commands found")
    }
  }

  def lookup(port: String) {
    val name = remote.broker.Service.name
    val host = "localhost"
    val path = s"akka.tcp://$name@$host:$port/user/broker"

    val system = ActorSystem("Sys", ConfigFactory.load("broker"))
    val client = system.actorOf(Service.props(path), "snd")

    //Use the system's dispatcher as ExecutionContext
    import system.dispatcher

    system.scheduler.schedule(0 seconds, 10 seconds, client, PING )
    system.scheduler.schedule(5 seconds,  5 seconds, client, GetDiff)
  }

  def props(path: String): Props = Props(new EdgeActor(path))

  val PING = "PING"
  val PONG = "PONG"

  case object GetDiff

  private case object Warmup
  case object Shutdown
  sealed trait Echo
  case object Start extends Echo
  case object Done extends Echo
}
