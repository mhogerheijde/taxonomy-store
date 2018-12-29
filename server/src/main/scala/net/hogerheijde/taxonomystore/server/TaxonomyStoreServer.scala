package net.hogerheijde.taxonomystore.server

import java.io.InputStream
import java.net.URI
import java.util.concurrent.TimeUnit

import scala.concurrent.ExecutionContext
import scala.util.Try

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import io.grpc.Server
import io.grpc.netty.GrpcSslContexts
import io.grpc.netty.NettyServerBuilder
import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.SslProvider
import net.hogerheijde.taxonomystore.api.TaxonomyStoreGrpc
import net.hogerheijde.taxonomystore.common.Files
import org.slf4j.LoggerFactory


object TaxonomyStoreServer {

  val Logger = LoggerFactory.getLogger(getClass)


  private val dtsCacheSize = 250

  def main(args: Array[String]): Unit = {
    TaxonomyStoreServer.build(
      Configuration.fromFile,
      ExecutionContext.global,
      dtsFilenameCache).map { server =>
      server.start()
      server.blockUntilShutdown(100L, TimeUnit.MINUTES) // scalastyle:ignore magic.number
    }.get // throw if something fishy happened!
  }

  def dtsFilenameCache: LoadingCache[Set[URI], Set[URI]] = {
    CacheBuilder.newBuilder()
      .maximumSize(dtsCacheSize)
      .build(
        new CacheLoader[Set[URI], Set[URI]] {
          override def load(entrypointSet: Set[URI]): Set[URI] = {
            val taxo = TaxonomyStoreImpl.loadDts(entrypointSet)
            taxo.taxonomyDocs.map(_.uri).toSet
          }
        }
      )
  }

  def build(
      configuration: Configuration,
      executionContext: ExecutionContext,
      cache: LoadingCache[Set[URI], Set[URI]]
  ): Try[TaxonomyStoreServer] = {
    Try {
      val server = NettyServerBuilder
        .forPort(configuration.server.port)
        .addService(TaxonomyStoreGrpc.bindService(new TaxonomyStoreImpl(cache), executionContext))
        .sslContext(sslContext(configuration))
        .build
      new TaxonomyStoreServer(server)
    }
  }



  def sslContext(config: Configuration): SslContext= {

    val certificate = Files.fromUri(config.server.certificate).getOrElse(
      throw new RuntimeException(s"Could not open file for ${config.server.certificate}"))
    val privateKey = Files.fromUri(config.server.privateKey).getOrElse(
      throw new RuntimeException(s"Could not open file for ${config.server.privateKey}"))

    val sslClientContextBuilder: SslContextBuilder = SslContextBuilder.forServer(
      certificate,
      privateKey)

    GrpcSslContexts.configure(sslClientContextBuilder, SslProvider.OPENSSL).build()
  }



}

class TaxonomyStoreServer private (server: Server) { self =>
  import TaxonomyStoreServer._

  private def start(): Unit = {
    server.start
    Logger.info("Server started, listening on " + server.getPort)
    sys.addShutdownHook {
      Logger.info("*** shutting down gRPC server since JVM is shutting down")
      self.stop()
      Logger.info("*** server shut down")
    }
  }

  private def stop(): Unit = {
    server.shutdown()
  }

  def blockUntilShutdown(timeout: Long, unit: TimeUnit): Boolean = { server.awaitTermination(timeout, unit) }

}
