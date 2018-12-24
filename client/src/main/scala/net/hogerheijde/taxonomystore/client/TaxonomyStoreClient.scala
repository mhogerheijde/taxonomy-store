package net.hogerheijde.taxonomystore.client


import java.util.concurrent.TimeUnit

import scala.util.Try

import eu.cdevreeze.tqa.docbuilder.jvm.PartialUriResolvers.PartialUriResolver
import io.grpc.ManagedChannel
import io.grpc.netty.GrpcSslContexts
import io.grpc.netty.NettyChannelBuilder
import io.netty.handler.ssl.SslContext
import net.hogerheijde.taxonomystore.api
import net.hogerheijde.taxonomystore.api.Document
import net.hogerheijde.taxonomystore.api.EntrypointSet
import net.hogerheijde.taxonomystore.api.TaxonomyStoreGrpc
import net.hogerheijde.taxonomystore.api.TaxonomyStoreGrpc.TaxonomyStoreBlockingClient
import org.slf4j.LoggerFactory


object TaxonomyStoreClient {

  val DEFAULT_HOST = "localhost"
  val DEFAULT_PORT = 50051

  private val LOGGER = LoggerFactory.getLogger(getClass)


  def sslContext: SslContext = {
    GrpcSslContexts
      .forClient
      .trustManager(getClass.getResourceAsStream("/certificates/ca.crt"))
      .build
  }

  def apply(host: String, port: Int): TaxonomyStoreClient = {
    val channel = NettyChannelBuilder
      .forAddress(host, port)
      .sslContext(sslContext)
      // TODO Find sensible size for largish taxonomies. (Or find a way to do on-the-fly size checking)
      .maxInboundMessageSize(500000000) // scalastyle:ignore magic.number
      .build
    new TaxonomyStoreClient(channel, TaxonomyStoreGrpc.blockingStub(channel))
  }

}

class TaxonomyStoreClient private(
    private val channel: ManagedChannel,
    private val blockingClient: TaxonomyStoreBlockingClient) {

  import TaxonomyStoreClient._

  def shutdown(): Unit = {
    channel.shutdown.awaitTermination(15, TimeUnit.MINUTES) // scalastyle:ignore magic.number
  }


  def documentForUri(uri: java.net.URI): Try[Document] = {
    val r = api.URI(uri.toString)
    Try { blockingClient.getDocument(r) } // FIXME do error handling
  }

  def dts(entrypointSet: Set[java.net.URI]): Try[PartialUriResolver] = {
    LOGGER.debug("Will try to get DTS for " + entrypointSet.mkString(", ") + " ...")
    val request = EntrypointSet(entrypointSet.map(_.toString).toIndexedSeq)
    Try {
      val response = blockingClient.getDts(request)
      LOGGER.info(s"File names (${response.files.length}) are:\n - " +
        response.files.map(_.fileuri).take(10).mkString("", "\n - ", "\n ... "))

      val filesByUri = response.files
        .map(f => (java.net.URI.create(f.fileuri), f))
        .toMap // Assume no duplicates

      resolver.PartialUriResolvers.memoryBacked(filesByUri)
    }
  }
}
