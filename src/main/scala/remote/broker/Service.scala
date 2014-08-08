package remote.broker

import com.typesafe.config.ConfigFactory
import akka.actor.{ ActorSystem, Props }

object Service {
  val name = "broker"

  def main(args: Array[String]) {
    var host = "localhost"
    var port = "2652"

    if (args.nonEmpty) {
      host = args(0)
      port = args(1)
    }

    System.setProperty("akka.remote.netty.tcp.hostname", host)
    System.setProperty("akka.remote.netty.tcp.port", port)

    start
  }

  def start() {
    // TODO: configと動的な値の合成方法
//    val config = ConfigFactory.load(name)
    val system = ActorSystem(name)
    val broker = system.actorOf(Props[BrokerActor], name)

    val host = System.getProperty("akka.remote.netty.tcp.hostname")
    val port = System.getProperty("akka.remote.netty.tcp.port")

    println(s"Started BrokerSystem(${broker.path}). Waiting for messages")
  }
}
