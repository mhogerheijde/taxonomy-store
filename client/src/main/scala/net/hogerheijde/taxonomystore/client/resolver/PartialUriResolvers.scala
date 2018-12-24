package net.hogerheijde.taxonomystore.client.resolver

import java.net.URI

import eu.cdevreeze.tqa.docbuilder.jvm.PartialUriResolvers.PartialUriResolver
import net.hogerheijde.taxonomystore.api.Document
import net.hogerheijde.taxonomystore.client.TaxonomyStoreClient
import org.xml.sax.InputSource

object PartialUriResolvers {

  def memoryBacked(filesByUri: Map[URI, Document]): PartialUriResolver = { uri =>
    filesByUri.get(uri) map { namedFile =>
      new InputSource(namedFile.contents.newInput())
    }
  }

  def storeBacked(client: TaxonomyStoreClient): PartialUriResolver = { uri =>
    client.documentForUri(uri).map { file =>
      new InputSource(file.contents.newInput())
    }.toOption // FIXME handle Try correctly
  }

}
