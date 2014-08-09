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

    val system = ActorSystem("edge", ConfigFactory.load("broker"))
    val client = system.actorOf(Service.props(path), "snd")

//    val client2 = system.actorOf(Service.props(path), "snd2")


    //Use the system's dispatcher as ExecutionContext
    import system.dispatcher
    import akka.pattern.ask

    for (
      i <- 1 to 1000
    ) yield {
      val future: Future[Any] = client.ask("hi")(10 seconds)
      future.map { res =>
        println(s"res: ${res}")
      }
      Thread.sleep(3000)
    }
  }

  def props(path: String): Props = Props(new EdgeActor(path))
}
