package remote.edge

import akka.actor.{ ActorSystem, Actor, ActorRef, Props, Cancellable }
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._

import akka.actor.Identify
import akka.actor.ActorIdentity
import akka.actor.ReceiveTimeout
import akka.actor.Terminated

trait Retry { this: Actor =>
  val remote: String
  val handshakeTimeout  = 3 seconds
  val heartbeatInterval = 5 seconds

  var lastRemoteActorRef: Option[ActorRef] = None
  var subscribedClasses = scala.collection.mutable.Set[Class[_]]()
  var scheduledJobs = scala.collection.mutable.Set[Cancellable]()

  import scala.concurrent.ExecutionContext.Implicits.global
  import context.system

  case object Heartbeat

  import akka.remote.{AssociatedEvent, AssociationErrorEvent, AssociationEvent, DisassociatedEvent, RemotingLifecycleEvent, QuarantinedEvent}
  override def preStart() {
    // super.preStart()
    system.eventStream.subscribe(self, classOf[RemotingLifecycleEvent])
    subscribedClasses  += classOf[RemotingLifecycleEvent]
    scheduledJobs += system.scheduler.schedule(0 seconds, heartbeatInterval, self, Heartbeat)
  }

  override def postStop() {
    subscribedClasses foreach(system.eventStream.unsubscribe(self, _))
    scheduledJobs foreach(_.cancel())
    scheduledJobs.clear()
    // super.postStop()
  }

  def receive = idle

  def attach(ref: ActorRef) {
    debug(s"接続しました: ${ref.path}")
    lastRemoteActorRef = Some(ref)
    context.setReceiveTimeout(Duration.Undefined)
    context.watch(ref)
    context.become(active(ref))
  }

  def detach() {
    debug("接続を破棄します")
    lastRemoteActorRef foreach { context.unwatch(_) }
    context.become(idle)
  }

  def idle: Receive = {
    case Heartbeat                   => handshake()
    case ActorIdentity(p, Some(ref)) => attach(ref)
    case ActorIdentity(p, None)      => debug(s"接続できません: $p")
    case ReceiveTimeout              => debug(s"応答がありません($handshakeTimeout): $remote")
  }

  def active(ref: ActorRef): Receive = {
    case Heartbeat  => // NOP

    // 相手が死んだ瞬間 (via RemotingLifecycleEvent)
    case e: DisassociatedEvent =>
      debug(s"コネクションが切断されました: ${e}")
      detach()

    // 完全に落ちてる時
    case e: AssociationErrorEvent =>
      debug(s"AssociationError: ${e}")

    // Actorが停止した (via context.watch(ref))
    case Terminated(`ref`) =>
      debug(s"Terminatedを受け取りました: ${ref.path}")
      detach()

    // Association to [akka.tcp://broker@localhost:2701] having UID [356031221] is irrecoverably failed. UID is now quarantined and all messages to this UID will be delivered to dead letters. Remote actorsystem must be restarted to recover from this situation.
    case QuarantinedEvent(address, uid) =>
      debug(s"隔離されました: $address $uid")

    case msg: RemotingLifecycleEvent =>
      debug(s"未知のイベント: ${msg}")

    case msg =>
      ref.forward(msg)
  }

  private def handshake() {
    lastRemoteActorRef match {
      case None => debug(s"接続します: $remote")
      case _    => debug(s"再接続します: $remote")
    }
    context.setReceiveTimeout(handshakeTimeout)
    context.actorSelection(remote) ! Identify(remote)
  }

  private def debug(msg: String) {
    println(s"[${self.path}] $msg")
  }
}
