package util

import java.net.InetAddress

case class RemoteAddress(host: String, port: Int) {
  def resolve: RemoteAddress = {
    host match {
      case "localhost" | "127.0.0.1" => copy(host = InetAddress.getLocalHost.getHostName)
      case _ => this
    }
  }

  def path(system: String, name: String) =
    s"akka.tcp://$system@$host:$port/user/$name"

  def configString: String =
    s"""
       | akka.remote.netty.tcp.hostname = ${host}
       | akka.remote.netty.tcp.port = ${port}
     """.stripMargin
}

object RemoteAddress {
  def zero = RemoteAddress("localhost", 0)

  def fromConfig(name: String): RemoteAddress = {
    import com.typesafe.config.ConfigFactory
    val conf = ConfigFactory.load(name)
    val host = conf.getString("akka.remote.netty.tcp.hostname")
    val port = conf.getInt("akka.remote.netty.tcp.port")
    RemoteAddress(host, port)
  }

  def parseWithDefault(addr: String, default: RemoteAddress): RemoteAddress = {
    try {
      val blank    = """\A\s*\Z""" r
      val hostPort = """\A([^:]+):(\d+)\Z""" r
      val hostOnly = """\A([^:]+)\Z""" r
      val portOnly = """\A:([^:]+)\Z""" r

      addr match {
        case null           => default
        case blank()        => default
        case hostPort(h, p) => default.copy(host = h, port = p.toInt)
        case hostOnly(h)    => default.copy(host = h)
        case portOnly(p)    => default.copy(port = p.toInt)
        case _ => throw new RuntimeException("host or port not found")
      }

    } catch {
      case e: Throwable =>
        throw new RuntimeException(s"RemoteAddress.unapply error: $e")
    }
  }
}
