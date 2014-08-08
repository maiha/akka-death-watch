package remote.edge

import akka.actor.{ ActorSystem, Actor, ActorRef, Props }
import com.typesafe.config.ConfigFactory
import scala.concurrent.{ Future, Await }
import scala.concurrent.duration._

import akka.actor.Identify
import akka.actor.ActorIdentity
import akka.actor.ReceiveTimeout
import akka.actor.Terminated

trait Reconnect { this: Actor =>
  import Reconnect._

  val path: String

  def receive = idle

  def attach(ref: ActorRef) {
    debug(s"connection established: ${ref.path}")
    context.watch(ref)
    context.become(running(ref))
    context.setReceiveTimeout(Duration.Undefined)
  }

  def detach() {
    debug("connection lost")
    context.become(idle)
  }

  def idle: Receive = {
    case Heartbeat =>
      debug("(heartbeat)")
      handshake()

    case ActorIdentity(`path`, Some(ref)) =>
      attach(ref)

    case ActorIdentity(`path`, None) =>
      debug(s"Remote actor not available: $path")

    case ReceiveTimeout =>
      self ! Heartbeat
  }

  def running(ref: ActorRef): Receive = {
    case Heartbeat =>
      debug("heartbeat: ok")

    case Terminated(`ref`) =>
      context.unwatch(ref)
      detach()

    case msg: Any =>
      ref.forward(msg)
  }

  private def handshake() {
    context.actorSelection(path) ! Identify(path)
  }

  private def debug(msg: String) {
    println(msg)
  }
}

object Reconnect {
  case object Heartbeat
}

