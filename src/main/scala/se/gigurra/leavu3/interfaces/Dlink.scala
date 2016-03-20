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
  @volatile var dlinkClient = RestClient(config.host, config.port)

  def start(appCfg: Configuration): Unit = {
    val dcsRemote = DcsRemote(appCfg)

    logger.info(s"Downloading datalink settings from dcs-remote ..")
    config = downloadDlinkConfig(dcsRemote)
    CfgUpdate.handleDlinkConfig(config)
    logger.info(s"Dlink settings downloaded:\n ${JSON.write(config)}")
    In.start()
    if (appCfg.relayDlink)
      Out.start()
    CfgUpdate.start(dcsRemote)
  }

  object CfgUpdate {

    def handleDlinkConfig(newConfig: DlinkConfiguration): Unit = {
      if (newConfig != config) {
        logger.info(s"Updating dlink settings to: \n ${JSON.write(newConfig)}")
        dlinkClient = RestClient(config.host, config.port)(dlinkClient.timer)
        config = newConfig
        In.onNewConfig()
        Out.onNewConfig()
      }
    }

    def start(dcsRemote: DcsRemote): Unit = {
      SimpleTimer(Duration.fromSeconds(3)) {
        Try(downloadDlinkConfig(dcsRemote)) match {
          case Success(newConfig) => handleDlinkConfig(newConfig)
          case Failure(e) => logger.warning(s"Unable to update data link configuration: $e")
        }
      }
    }
  }

  object In {

    @volatile var snapshot: Map[String, DlinkData] = Map.empty

    def onNewConfig(): Unit = {
      snapshot = Map.empty
    }

    def start(): Unit = {

      SimpleTimer.fromFps(config.inFps) {
        Try {
          val rawData = JSON.readMap(dlinkClient.getBlocking(config.team, cacheMaxAgeMillis = Some(10000L)))
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
    def addMark(mark: Mark): Unit = marks += mark.id -> mark
    def deleteMark(id: String): Unit = marks -= id

    def onNewConfig(): Unit = {
      marks = Map.empty
    }

    def start(): Unit = {

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
          Try(dlinkClient.putBlocking(s"${config.team}/${config.callsign}", json)) match {
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
