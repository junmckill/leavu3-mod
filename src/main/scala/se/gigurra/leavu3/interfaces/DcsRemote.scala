package se.gigurra.leavu3.interfaces

import com.twitter.util.Await
import se.gigurra.leavu3.datamodel.Configuration
import se.gigurra.leavu3.util.RestClient

import scala.language.implicitConversions
import scala.util.control.NonFatal

case class DcsRemote private (config: Configuration) {
  val client = RestClient(config.dcsRemoteAddress, config.dcsRemotePort)
  try Await.result(client.get(s"static-data")) catch {
    case NonFatal(e) => throw new RuntimeException(s"Failed to communicate with dcs remote!", e)
  }
}

/**
  * Created by kjolh on 3/20/2016.
  */

object DcsRemote {

  private var instance: DcsRemote = null

  def init(appCfg: Configuration): Unit = {
    instance = DcsRemote(appCfg)
  }

  implicit def remote2client(r: DcsRemote.type): RestClient = instance.client
}
