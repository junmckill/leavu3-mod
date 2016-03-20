package se.gigurra.leavu3.interfaces

import com.twitter.finagle.FailedFastException
import com.twitter.util.Duration
import se.gigurra.heisenberg.MapDataParser
import se.gigurra.leavu3.datamodel.{Configuration, DlinkConfiguration, DlinkData, Mark, Member}
import se.gigurra.leavu3.util.{RestClient, SimpleTimer}
import se.gigurra.serviceutils.json.JSON
import se.gigurra.serviceutils.twitter.logging.Logging
import se.gigurra.serviceutils.twitter.service.ServiceException

import scala.util.{Failure, Success, Try}

/**
  * Created by kjolh on 3/20/2016.
  */
object Dlink extends Logging {

  @volatile var config: DlinkConfiguration = DlinkConfiguration()
  @volatile var snapshot: Map[String, DlinkData] = Map.empty

  def start(dcsRemote: DcsRemote, relayDlink: Boolean): Unit = {
    logger.info(s"Downloading datalink settings from dcs-remote ..")
    config = downloadDlinkConfig(dcsRemote)
    logger.info(s"Dlink settings downloaded:\n ${JSON.write(config)}")
    In.start(config)
    if (relayDlink)
      Out.start(config)
    CfgUpdate.start(config)
  }

  object CfgUpdate {
    def start(config: DlinkConfiguration): Unit = {
      SimpleTimer(Duration.fromSeconds(3)) {
        //TODO: REMOVE logging AFTER IMPLEMENTING
        // TODO: On Change, delete all marks!
        logger.info(s"Updating dlink settings")
      }
    }
  }

  object In {

    def start(config: DlinkConfiguration): Unit = {

      val client = RestClient(config.host, config.port)

      SimpleTimer.fromFps(config.inFps) {
        Try {
          val rawData = JSON.readMap(client.getBlocking(config.team, cacheMaxAgeMillis = Some(10000L)))
          snapshot = rawData.collect {
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

  object Out extends Logging {

    @volatile private var marks: Map[String, Mark] = Map.empty

    def hasMark(id: String): Boolean = marks.contains(id)

    def addMark(mark: Mark): Unit = {
      marks += mark.id -> mark
    }

    def deleteMark(id: String): Unit = {
      marks -= id
    }

    def start(config: DlinkConfiguration): Unit = {
      val client = RestClient(config.host, config.port)

      SimpleTimer.fromFps(config.outFps) {
        val source = GameIn.snapshot
        if (source.err.isEmpty && source.age < 3.0) {
          val self = Member.marshal(
            Member.planeId -> source.metaData.planeId,
            Member.modelTime -> source.metaData.modelTime,
            Member.position -> source.selfData.position,
            Member.velocity -> source.flightModel.velocity,
            Member.targets -> source.sensors.targets.locked,
            Member.selfData -> source.metaData.selfData,
            Member.markPos -> marks
          )
          val json = JSON.write(self)
          Try(client.putBlocking(s"${config.team}/${config.callsign}", json)) match {
            case Success(_) =>
            case Failure(e: ServiceException) =>
              logger.error(s"Data link host replied with an error: $e")
            case Failure(e) =>
              logger.error(e, s"Unexpected error when attempting to send to dlink")
          }
        }
      }
    }
  }

  private def downloadDlinkConfig(dcsRemote: DcsRemote): DlinkConfiguration = {
    Try(dcsRemote.getBlocking(s"static-data/dlink-settings")) match {
      case Success(data) => JSON.read[DlinkConfiguration](data)
      case Failure(e) => throw new RuntimeException(s"Could not download data link configuration from dcs-remote", e)
    }
  }

}
