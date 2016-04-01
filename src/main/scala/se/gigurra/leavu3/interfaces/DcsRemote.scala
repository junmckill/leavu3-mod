package se.gigurra.leavu3.interfaces

import com.twitter.finagle.FailedFastException
import com.twitter.util.{Await, Future}
import se.gigurra.leavu3.datamodel.{Configuration, DcsRemoteRemoteConfig}
import se.gigurra.leavu3.util.{DefaultTimer, IdenticalRequestPending, RestClient}
import se.gigurra.serviceutils.json.JSON
import se.gigurra.serviceutils.twitter.logging.Logging

import scala.language.implicitConversions
import scala.util.control.NonFatal

case class DcsRemote private(config: Configuration) extends Logging {

  val client = RestClient(config.dcsRemoteAddress, config.dcsRemotePort, "Dcs Remote")
  @volatile var remoteConfig = initialDownloadConfig()

  DefaultTimer.seconds(3) {
    downloadConfig()
      .onSuccess { staticData =>
        remoteConfig = staticData
      }
      .onFailure {
        case e: IdenticalRequestPending =>
        case e: FailedFastException =>
        case e => logger.warning(s"Unable to download configuration from Dcs Remote: $e")
      }
  }

  private def initialDownloadConfig(): DcsRemoteRemoteConfig = {
    try Await.result(downloadConfig()) catch {
      case NonFatal(e) => throw new RuntimeException(s"Failed to communicate with dcs remote!", e)
    }
  }

  private def downloadConfig(): Future[DcsRemoteRemoteConfig] = {
    client.get(s"static-data").map(JSON.read[DcsRemoteRemoteConfig])
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

  def remoteConfig: DcsRemoteRemoteConfig = instance.remoteConfig
}
