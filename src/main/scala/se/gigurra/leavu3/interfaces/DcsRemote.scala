package se.gigurra.leavu3.interfaces

import java.util.UUID

import com.twitter.finagle.FailedFastException
import com.twitter.util.{Await, Future}
import se.gigurra.heisenberg.MapData.SourceData
import se.gigurra.heisenberg._
import se.gigurra.leavu3.datamodel.DlinkData._
import se.gigurra.leavu3.datamodel.{Configuration, DcsRemoteRemoteConfig, Leavu3Instance}
import se.gigurra.leavu3.util.{DefaultTimer, IdenticalRequestPending, RestClient}
import se.gigurra.serviceutils.json.JSON
import se.gigurra.serviceutils.twitter.logging.Logging

import scala.language.implicitConversions
import scala.util.control.NonFatal

case class DcsRemote private(config: Configuration) extends Logging {
  import DcsRemote._

  private val ownInstanceId = UUID.randomUUID().toString
  private val client = RestClient(config.dcsRemoteAddress, config.dcsRemotePort, "Dcs Remote")
  private val cache = new scala.collection.concurrent.TrieMap[String, Map[String, Stored[_]]]
  @volatile var remoteConfig = initialDownloadConfig()
  @volatile var ownPriority: Int = 0

  DefaultTimer.fps(1) {
    downloadUpdatedConfig()
      .onSuccess { staticData =>
        remoteConfig = staticData
      }
      .onFailure {
        case e: IdenticalRequestPending =>
        case e: FailedFastException =>
        case e => logger.warning(s"Unable to download configuration from Dcs Remote: $e")
      }
  }

  DefaultTimer.fps(config.gameDataFps / 2) {
    registerLeavu3Instance()
      .onFailure {
        case e: IdenticalRequestPending =>
        case e: FailedFastException =>
        case e => logger.warning(s"Unable to register Leavu3 instance on Dcs Remote: $e")
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
  def loadStatic[T: MapParser](category: String, maxAge: Option[Long]): Map[String, Stored[T]] = {

    client.get(category, maxAge = maxAge).map { data =>

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
    try Await.result(downloadUpdatedConfig()) catch {
      case NonFatal(e) => throw new RuntimeException(s"Failed to communicate with dcs remote!", e)
    }
  }

  private def downloadUpdatedConfig(): Future[DcsRemoteRemoteConfig] = {
    client.get(s"static-data").map(JSON.read[DcsRemoteRemoteConfig])
  }

  private def registerLeavu3Instance(): Future[Unit] = {
    store("leavu3-instances", ownInstanceId)(Leavu3Instance(ownInstanceId, ownPriority, isActingMaster))
  }

  def isActingMaster: Boolean = {

    val instanceLkup = loadStatic[Leavu3Instance]("leavu3-instances", maxAge = Some(1000L))

    instanceLkup.get(ownInstanceId) match {
      case None => true
      case Some(myInstance) =>
        val instances: Seq[Leavu3Instance] = instanceLkup.values.map(_.item).toSeq.sortBy(_.id)
        val highestPriority = instances.map(_.priority).max
        val instancesWithHighestPrio = instances.filter(_.priority == highestPriority)
        if (instancesWithHighestPrio.size == 1) {
          instancesWithHighestPrio.head.id == ownInstanceId
        } else {
          instances.head.id == ownInstanceId
        }
    }
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
