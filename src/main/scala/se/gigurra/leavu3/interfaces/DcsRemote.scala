package se.gigurra.leavu3.interfaces

import java.util.UUID

import com.twitter.finagle.FailedFastException
import com.twitter.util.{Await, Future}
import se.gigurra.heisenberg.MapData.SourceData
import se.gigurra.heisenberg._
import se.gigurra.leavu3.datamodel.DlinkData._
import se.gigurra.leavu3.datamodel.{Configuration, DcsRemoteRemoteConfig, Member, SafeParsed}
import se.gigurra.leavu3.util.{CurTime, DefaultTimer, IdenticalRequestPending, RestClient}
import se.gigurra.serviceutils.json.JSON
import se.gigurra.serviceutils.twitter.logging.Logging

import scala.language.implicitConversions
import scala.util.control.NonFatal

case class DcsRemote private(config: Configuration) extends Logging {
  import DcsRemote._

  val myId = UUID.randomUUID().toString
  val client = RestClient(config.dcsRemoteAddress, config.dcsRemotePort, "Dcs Remote")
  val cache = new scala.collection.concurrent.TrieMap[String, Map[String, Stored[_]]]
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

  def store[T: MapProducer](category: String, id: String)(data: => T): Future[Unit] = {
    client.put(s"$category/$id")(JSON.write(data))
  }

  def delete(category: String, id: String): Future[Unit] = {
    client.delete(s"$category/$id")
  }

  /**
    * Load will always be empty on the first attempt,
    * since it triggers the actual download from the Dcs Remote
    */
  def loadStatic[T: MapParser](category: String): Map[String, Stored[T]] = {

    client.get(category, maxAge = Some(Int.MaxValue)).map { data =>

      val rawData = JSON.readMap(data).asInstanceOf[Map[String, SourceData]].map {
        case (k, v) => k ->
          Stored[T](
            timestamp = v("timestamp").asInstanceOf[Double],
            age = v("age").asInstanceOf[Double],
            item = MapParser.parse[T](v("data").asInstanceOf[Map[String, SourceData]])
          )
      }

      cache.put(category, rawData)

    }.onFailure {
      case e: IdenticalRequestPending =>
      case e: FailedFastException =>
      case NonFatal(e) => logger.warning(s"Unable to download and process category $category from local Dcs Remote: $e")
    }

    cache.get(category).map(_.asInstanceOf[Map[String, Stored[T]]]).getOrElse(Map.empty[String, Stored[T]])
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
  implicit def remote2remote(r: DcsRemote.type): DcsRemote = instance

  def remoteConfig: DcsRemoteRemoteConfig = instance.remoteConfig

  case class Stored[T](timestamp: Double, age: Double, item: T)

}
