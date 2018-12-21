package net.hogerheijde.taxonomystore.server

import java.io.FileInputStream
import java.io.InputStream
import java.net.URI
import java.nio.charset.StandardCharsets

import scala.collection.immutable.IndexedSeq
import scala.concurrent.Future
import scala.io.Source

import com.google.protobuf.ByteString
import eu.cdevreeze.tqa.base.relationship.DefaultRelationshipFactory
import eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy
import eu.cdevreeze.tqa.base.taxonomybuilder
import eu.cdevreeze.tqa.docbuilder
import eu.cdevreeze.tqa.docbuilder.SimpleCatalog
import eu.cdevreeze.tqa.docbuilder.jvm.UriConverters
import net.hogerheijde.taxonomystore.api.DtsReply
import net.hogerheijde.taxonomystore.api.DtsRequest
import net.hogerheijde.taxonomystore.api.NamedFile
import net.hogerheijde.taxonomystore.api.TaxonomyStoreGrpc
import net.hogerheijde.taxonomystore.server.TaxonomyStoreServer.Logger
import net.hogerheijde.taxonomystore.server.util.Timer
import net.hogerheijde.taxonomystore.server.util.Timer.TimedResult
import net.sf.saxon.s9api.Processor

private[server] class TaxonomyStoreImpl extends TaxonomyStoreGrpc.TaxonomyStore {

  import TaxonomyStoreImpl._

  override def dts(req: DtsRequest): Future[DtsReply] = {
    Logger.debug(s"Serving dts request for ${req.entrypoints}")


    val TimedResult(taxo, duration) = Timer { TaxonomyStoreImpl.loadDts(req.entrypoints.map(URI.create).toSet) }
    Logger.debug(s"Loaded DTS in ${duration}")
    val entrypointFiles = req.entrypoints.map(URI.create(_)).map(uriConverter(_))
    val dtsFiles = taxo.taxonomyDocs.map(td =>
      NamedFile(
        td.uri.toString,
        ByteString.readFrom(inputStreamFor(td.uri)),
        "application/xml; charset=UTF-8"
      )
    )

    Logger.debug(s"We've got ${dtsFiles.length} files to transmit")
    val reply = DtsReply(files = dtsFiles)
    Future.successful(reply)
  }
}

object TaxonomyStoreImpl {
  val nltaxonomie = SimpleCatalog.UriRewrite(None, "http://www.nltaxonomie.nl/", "file:/opt/taxonomy/www.nltaxonomie.nl/")
  val xbrl = SimpleCatalog.UriRewrite(None, "http://www.xbrl.org/", "file:/opt/taxonomy/www.xbrl.org/")
  val w3c = SimpleCatalog.UriRewrite(None, "http://www.w3c.org/", "file:/opt/taxonomy/www.w3c.org/")

  val docCacheSize: Int = 10000

  val uriConverter = UriConverters.fromCatalogFallingBackToIdentity(
    SimpleCatalog(None, IndexedSeq(w3c, xbrl, nltaxonomie))
  )

  val processor = new Processor(false)

  def inputStreamFor(taxoFileUri: URI): InputStream = {
    uriConverter(taxoFileUri).toURL.openStream()
  }

  def loadDts(entrypointUris: Set[URI]): BasicTaxonomy = {

    val docBuilder = new docbuilder.saxon.SaxonDocumentBuilder(
      processor.newDocumentBuilder(),
      docbuilder.jvm.UriResolvers.fromUriConverter(uriConverter))

    val documentBuilder = new docbuilder.jvm.CachingDocumentBuilder(
      docbuilder.jvm.CachingDocumentBuilder.createCache(docBuilder, docCacheSize))

    val documentCollector = taxonomybuilder.DefaultDtsCollector()

    val relationshipFactory = DefaultRelationshipFactory.StrictInstance

    val taxoBuilder =
      taxonomybuilder.TaxonomyBuilder.
        withDocumentBuilder(documentBuilder).
        withDocumentCollector(documentCollector).
        withRelationshipFactory(relationshipFactory)

    taxoBuilder.build(entrypointUris)
  }
}
