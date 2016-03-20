package se.gigurra.leavu3.interfaces

import se.gigurra.leavu3.datamodel.Configuration
import se.gigurra.leavu3.util.RestClient

import scala.language.implicitConversions
import scala.util.control.NonFatal

/**
  * Created by kjolh on 3/20/2016.
  */
case class DcsRemote(config: Configuration) {
  val client = RestClient(config.dcsRemoteAddress, config.dcsRemotePort)
  try client.getBlocking(s"static-data") catch {
    case NonFatal(e) => throw new RuntimeException(s"Failed to communicate with dcs remote!", e)
  }
}

object DcsRemote {
  implicit def remote2client(r: DcsRemote): RestClient = r.client
}
