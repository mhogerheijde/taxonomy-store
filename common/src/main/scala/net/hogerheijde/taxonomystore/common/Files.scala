package net.hogerheijde.taxonomystore.common

import java.io.InputStream
import java.net.URI

object Files {


  def fromUri(uri: URI, allowedSchemes: Set[String] = Set("file", "classpath")): Option[InputStream] = {

    if (allowedSchemes.contains(uri.getScheme)) {
      if (uri.getScheme == "classpath") {
        Option(getClass.getResourceAsStream(uri.getPath))
      } else {
        Option(uri.toURL.openStream())
      }
    } else {
      None
    }

  }
}
