package net.hogerheijde.taxonomystore.client

import java.net.URI

import net.hogerheijde.taxonomystore.client.TaxonomyStoreClient.DEFAULT_HOST
import net.hogerheijde.taxonomystore.client.TaxonomyStoreClient.DEFAULT_PORT

object Application {

  def main(args: Array[String]): Unit = {

    val client = TaxonomyStoreClient.apply(DEFAULT_HOST, DEFAULT_PORT)

    client.dts(Set(
      URI.create("http://www.nltaxonomie.nl/nt12/kvk/20171213/entrypoints/kvk-rpt-jaarverantwoording-2017-nlgaap-klein.xsd")))

  }

}
