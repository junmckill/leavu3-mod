package se.gigurra.leavu3.externaldata

import se.gigurra.heisenberg.MapDataParser
import se.gigurra.leavu3.util.{RestClient, SimpleTimer}
import se.gigurra.leavu3.{Configuration, DlinkData}
import se.gigurra.serviceutils.json.JSON
import se.gigurra.serviceutils.twitter.logging.Logging
import se.gigurra.serviceutils.twitter.service.ServiceException

import scala.util.{Failure, Success, Try}

object DlinkInData extends Logging {

  def startPoller(config: Configuration): Unit = {

    val client = RestClient(config.dlinkHost, config.dlinkPort)

    SimpleTimer.fromFps(config.dlinkInFps) {
      Try {
        val rawData = JSON.readMap(client.getBlocking(config.dlinkTeam, cacheMaxAgeMillis = Some(10000L)))
        ExternalData.dlinkIn = rawData.map {
          case (id, raw) =>
            id -> implicitly[MapDataParser[DlinkData]].parse(raw)
        }
      } match {
        case Success(_) =>
        case Failure(e: ServiceException) =>
          logger.error(s"Data link host replied with an error: $e")
        case Failure(e) =>
          logger.error(s"Unexpected error when attempting to receive from dlink: $e", e)
      }
    }
  }
}