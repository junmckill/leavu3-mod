package se.gigurra.leavu3.interfaces

import com.twitter.finagle.FailedFastException
import se.gigurra.heisenberg.MapDataParser
import se.gigurra.leavu3.datamodel.{DlinkConfiguration, DlinkData}
import se.gigurra.leavu3.util.{RestClient, SimpleTimer}
import se.gigurra.serviceutils.json.JSON
import se.gigurra.serviceutils.twitter.logging.Logging
import se.gigurra.serviceutils.twitter.service.ServiceException

import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}

object DlinkIn extends Logging {

  def startPoller(config: DlinkConfiguration): Unit = {

    val client = RestClient(config.host, config.port)

    SimpleTimer.fromFps(config.inFps) {
      Try {
        val rawData = JSON.readMap(client.getBlocking(config.team, cacheMaxAgeMillis = Some(10000L)))
        Snapshots.dlinkIn = rawData.collect {
          case ValidDlinkData(id, dlinkData) => id -> dlinkData
        }
      } match {
        case Success(_) =>
        case Failure(e: ServiceException) =>
          logger.error(s"Data link host replied with an error: $e")
        case Failure(e: FailedFastException) => // Ignore ..
        case Failure(e) =>
          logger.error(e, s"Unexpected error when attempting to receive from dlink")
      }
    }
  }

  object ValidDlinkData {
    def unapply(callsignAndData: (String, Any)): Option[(String, DlinkData)] = {
      val callsign = callsignAndData._1
      val raw = callsignAndData._2
      Try(implicitly[MapDataParser[DlinkData]].parse(raw)) match {
        case Success(dlinkData) => Some(callsign, dlinkData)
        case Failure(e) =>
          logger.warning(s"Failed to read dlink data from $callsign")
          None
      }
    }
  }

}