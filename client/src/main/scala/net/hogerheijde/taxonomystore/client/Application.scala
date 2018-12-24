package net.hogerheijde.taxonomystore.client

import java.net.URI

import scala.collection.immutable.IndexedSeq

import eu.cdevreeze.tqa.base.relationship.DefaultRelationshipFactory
import eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy
import eu.cdevreeze.tqa.base.taxonomybuilder
import eu.cdevreeze.tqa.docbuilder
import eu.cdevreeze.tqa.docbuilder.jvm.UriResolvers
import eu.cdevreeze.tqa.docbuilder.jvm.UriResolvers.UriResolver
import net.hogerheijde.taxonomystore.client.TaxonomyStoreClient.DEFAULT_HOST
import net.hogerheijde.taxonomystore.client.TaxonomyStoreClient.DEFAULT_PORT
import net.hogerheijde.taxonomystore.common.Timer
import net.hogerheijde.taxonomystore.common.Timer.TimedResult
import net.sf.saxon.s9api.Processor
import org.slf4j.LoggerFactory

object Application {

  val Logger = LoggerFactory.getLogger(getClass)

  val processor = new Processor(false)
  val docCacheSize = 10000

  def main(args: Array[String]): Unit = {

    val client = TaxonomyStoreClient.apply(DEFAULT_HOST, DEFAULT_PORT)

    val uriResolver = UriResolvers.fromPartialUriResolversWithoutFallback(IndexedSeq(
      resolver.PartialUriResolvers.storeBacked(client)
    ))

    val entrypointSet = Set(URI.create("http://www.nltaxonomie.nl/nt12/kvk/20171213/entrypoints/kvk-rpt-jaarverantwoording-2017-nlgaap-groot.xsd"))

    fetchDtsResolver(client, entrypointSet)
    fetchDtsResolver(client, entrypointSet)
    val memoryResolver = fetchDtsResolver(client, entrypointSet)

    val TimedResult(taxoFromMem, memDuration) = Timer {
      loadDts(entrypointSet,
        memoryResolver,
      )
    }
    Logger.info(s"DTS from memory ${taxoFromMem.taxonomyDocs.length}, took ${memDuration}")

    val TimedResult(taxo, duration) = Timer { loadDts(entrypointSet,
      uriResolver,
    ) }
    Logger.info(s"DTS per file ${taxo.taxonomyDocs.length}, took ${duration}")

  }

  def fetchDtsResolver(client: TaxonomyStoreClient, entrypoints: Set[URI]): UriResolver = {
    val TimedResult(memoryResolver, fetchResolver) = Timer {
      val partial = client.dts(entrypoints).get
      UriResolvers.fromPartialUriResolversWithoutFallback(IndexedSeq(partial))
    }
    Logger.info(s"Fetched taxonomy document resolver in ${fetchResolver}")
    memoryResolver
  }


  def loadDts(entrypointUris: Set[URI], uriResolver: UriResolver): BasicTaxonomy = {



    val docBuilder = new docbuilder.saxon.SaxonDocumentBuilder(
      processor.newDocumentBuilder(),
      uriResolver)

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
