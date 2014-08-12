package client

import akka.actor.{ ActorSystem, Actor, ActorRef, Props }
import com.typesafe.config.ConfigFactory
import scala.concurrent.{ Future, Await }
import scala.concurrent.duration._
import scala.language.postfixOps

object Sender {
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
    val client = system.actorOf(Sender.props(path), "snd")

    //Use the system's dispatcher as ExecutionContext
    import system.dispatcher

    system.scheduler.schedule(0 seconds, 10 seconds, client, PING )
    system.scheduler.schedule(5 seconds,  5 seconds, client, GetDiff)
  }

  def props(path: String): Props = Props(new Sender(path))

  val PING = "PING"
  val PONG = "PONG"

  case object GetDiff

  private case object Warmup
  case object Shutdown
  sealed trait Echo
  case object Start extends Echo
  case object Done extends Echo
}

class Sender(path: String) extends Actor {
  import Sender._

  import akka.actor.Identify
  import akka.actor.ActorIdentity
  import akka.actor.ReceiveTimeout
  import akka.actor.Terminated

  context.setReceiveTimeout(3.seconds)
  sendIdentifyRequest()
    
  def sendIdentifyRequest() { context.actorSelection(path) ! Identify(path) }
  def receive = identifying

  def identifying: Receive = {
    case GetDiff =>
      debug("got GetDiff")

    case PING =>
      debug("got PING")

    case ActorIdentity(`path`, Some(ref)) =>
      debug("identified")
      context.watch(ref)
      context.become(active(ref))
      context.setReceiveTimeout(Duration.Undefined)
      self ! Start
    case ActorIdentity(`path`, None) =>
      debug(s"Remote actor not available: $path")
    case ReceiveTimeout              => sendIdentifyRequest()
  }

  def active(ref: ActorRef): Receive = {
    case GetDiff =>
      getDiff(ref)

    case PING =>
      debug("got PING")

    case Start =>
      sendlog(ref, "start")
      ref ! "start"

    case Done =>
      sendlog(ref, "done")
      ref ! "done"

    case Terminated(`ref`) =>
      println("Receiver terminated")
      context.system.shutdown()
  }

  private def getDiff(ref: ActorRef) {
    sendlog(ref, "GetDiff")
    ref ! "GetDiff"
  }

  private def sendlog(ref: ActorRef, arg: Any) {
    val msg = s"from: [${self.path}]\nto: [${ref.path}]\n${arg}"
    debug(msg)
  }

  private def debug(msg: String) {
    println(s"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
    println(msg)
    println(s"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
  }
}
