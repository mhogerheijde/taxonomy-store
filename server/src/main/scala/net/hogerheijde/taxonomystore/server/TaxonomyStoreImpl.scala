package net.hogerheijde.taxonomystore.server

import java.io.InputStream
import java.net.URI

import scala.collection.immutable.IndexedSeq
import scala.concurrent.Future

import com.google.common.cache.LoadingCache
import com.google.protobuf.ByteString
import eu.cdevreeze.tqa.base.relationship.DefaultRelationshipFactory
import eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy
import eu.cdevreeze.tqa.base.taxonomybuilder
import eu.cdevreeze.tqa.docbuilder
import eu.cdevreeze.tqa.docbuilder.SimpleCatalog
import eu.cdevreeze.tqa.docbuilder.jvm.UriConverters
import net.hogerheijde.taxonomystore.api
import net.hogerheijde.taxonomystore.api.Document
import net.hogerheijde.taxonomystore.api.Dts
import net.hogerheijde.taxonomystore.api.EntrypointSet
import net.hogerheijde.taxonomystore.api.TaxonomyStoreGrpc
import net.hogerheijde.taxonomystore.common.Timer
import net.hogerheijde.taxonomystore.common.Timer.TimedResult
import net.sf.saxon.s9api.Processor
import org.slf4j.LoggerFactory

private[server] class TaxonomyStoreImpl(dtsFilenameCache: LoadingCache[Set[URI], Set[URI]]) extends TaxonomyStoreGrpc.TaxonomyStore {

  import TaxonomyStoreImpl._

  override def getDts(req: EntrypointSet): Future[Dts] = {
    Logger.trace(s"Serving dts request for ${req.entrypoints}")
    val TimedResult(taxoFileUris, duration) = Timer { dtsFilenameCache.get(req.entrypoints.map(URI.create).toSet) }
    Logger.trace(s"Loaded DTS in ${duration}")
    val dtsFiles = taxoFileUris.map(uri => getDocumentByUri(uri))
    Logger.trace(s"We've got ${dtsFiles.size} files to transmit")
    val reply = Dts(files = dtsFiles.toIndexedSeq)
    Future.successful(reply)
  }

  override def getDocument(request: api.URI): Future[Document] = {
      Future.successful(getDocumentByUri(URI.create(request.uri))) // FIXME error handling
  }

  private def getDocumentByUri(uri: URI): Document = {
    Document(
        uri.toString,
        ByteString.readFrom(inputStreamFor(uri)),
        "application/xml; charset=UTF-8" // FIXME we need to know the mimeType
      )
  }
}

object TaxonomyStoreImpl {
  val nltaxonomie = SimpleCatalog.UriRewrite(None, "http://www.nltaxonomie.nl/", "file:/opt/taxonomy/www.nltaxonomie.nl/")
  val xbrl = SimpleCatalog.UriRewrite(None, "http://www.xbrl.org/", "file:/opt/taxonomy/www.xbrl.org/")
  val w3c = SimpleCatalog.UriRewrite(None, "http://www.w3c.org/", "file:/opt/taxonomy/www.w3c.org/")

  val docCacheSize: Int = 10000

  val Logger = LoggerFactory.getLogger(getClass)

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
