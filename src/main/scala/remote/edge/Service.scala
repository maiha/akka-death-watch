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
    val host   = "localhost"
    val remote = s"akka.tcp://broker@$host:$port/user/broker"

    val system = ActorSystem("edge", ConfigFactory.load("edge"))
    val client = system.actorOf(Service.props(remote), "edge")

//    val client2 = system.actorOf(Service.props(remote), "snd2")


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

  def props(remote: String): Props = Props(new EdgeActor(remote))
}
