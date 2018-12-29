package net.hogerheijde.taxonomystore.server

import java.net.URI

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import net.hogerheijde.taxonomystore.server.Configuration.Server

case class Configuration(
    server: Server,
    config: Config,
)


object Configuration {

  case class Server(
    privateKey: URI,
    certificate: URI,
    port: Int,
  )


  def fromFile: Configuration = {
    val config = ConfigFactory.load()
    config.checkValid(ConfigFactory.defaultReference(), "server")

    val certificate = try {
      URI.create(config.getString("server.certificate"))
    } catch {
      case _ =>
        throw new RuntimeException(
          "Expected `server.certificate` to be a URI with either scheme `file:` or `classpath:`." +
            s"found ${config.getString("server.certificate")}"
        )
    }

    val privateKey = try {
      URI.create(config.getString("server.privateKey"))
    } catch {
      case _ =>
        throw new RuntimeException(
          "Expected `server.privateKey` to be a URI with either scheme `file:` or `classpath:`." +
            s"found ${config.getString("server.privateKey")}"
        )
    }

    Configuration(Server(privateKey, certificate, config.getInt("server.port")), config)
  }
}
