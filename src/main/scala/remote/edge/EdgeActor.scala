package remote.edge

import akka.actor.{ ActorSystem, Actor, ActorRef, Props }
import com.typesafe.config.ConfigFactory
import scala.concurrent.{ Future, Await }
import scala.concurrent.duration._

import akka.actor.Identify
import akka.actor.ActorIdentity
import akka.actor.ReceiveTimeout
import akka.actor.Terminated

class EdgeActor(path: String) extends Actor {
  import Service._

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
