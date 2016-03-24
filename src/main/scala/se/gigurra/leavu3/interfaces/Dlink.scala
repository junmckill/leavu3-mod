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
  @volatile var connected = false

  def start(appCfg: Configuration): Unit = {

    logger.info(s"Downloading datalink settings from dcs-remote ..")
    CfgUpdate.handleDlinkConfig(downloadDlinkConfig())
    logger.info(s"Dlink settings downloaded:\n ${JSON.write(config)}")
    In.start()
    if (appCfg.relayDlink)
      Out.start()
    CfgUpdate.start()
  }

  object CfgUpdate {

    def handleDlinkConfig(newConfig: DlinkConfiguration): Unit = {
      if (newConfig != config) {
        logger.info(s"Updating dlink settings to: \n ${JSON.write(newConfig)}")
        dlinkClient = RestClient(newConfig.host, newConfig.port)(dlinkClient.timer)
        config = newConfig
        In.onNewConfig()
        Out.onNewConfig()
      }
    }

    def start(): Unit = {
      SimpleTimer(Duration.fromSeconds(3)) {
        Try(downloadDlinkConfig()) match {
          case Success(newConfig) => Try(handleDlinkConfig(newConfig)) match {
            case Success(_) =>
            case Failure(e) =>
              logger.error(s"Unable to update data link configuration: $e")
          }
          case Failure(e) => logger.warning(s"Unable to update data link configuration: $e")
        }
      }
    }
  }

  object In {

    @volatile var ownTeam: Map[String, DlinkData] = Map.empty
    @volatile var allTeams: Map[Int, Map[String, DlinkData]] = Map.empty

    def notPlaying: Map[String, DlinkData] = allTeams.getOrElse(0, Map.empty)
    def blue: Map[String, DlinkData] = allTeams.getOrElse(2, Map.empty)
    def red: Map[String, DlinkData] = allTeams.getOrElse(1, Map.empty)

    def onNewConfig(): Unit = {
      ownTeam = Map.empty
      allTeams = Map.empty
    }

    def start(): Unit = {

      SimpleTimer.fromFps(1) {
        Try {
          val rawData = JSON.readMap(dlinkClient.getBlocking(config.team, cacheMaxAgeMillis = Some(10000L)))
          val everyoneOnNetwork = rawData.collect { case ValidDlinkData(id, dlinkData) => id -> dlinkData }
          allTeams = everyoneOnNetwork.groupBy(_._2.selfData.coalitionId)
          ownTeam = allTeams.getOrElse(GameIn.snapshot.selfData.coalitionId, Map.empty)
          connected = true
        } match {
          case Success(_) =>
          case Failure(e: ServiceException) =>
            connected = false
            allTeams = Map.empty
            ownTeam = Map.empty
            logger.error(s"Data link host replied with an error: $e")
          case Failure(e: FailedFastException) =>
            connected = false
            allTeams = Map.empty
            ownTeam = Map.empty
          // Ignore ..
          case Failure(e) =>
            connected = false
            allTeams = Map.empty
            ownTeam = Map.empty
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

    def sameTeam(data: DlinkData): Boolean = {
      data.selfData.coalitionId == GameIn.snapshot.selfData.coalitionId
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

      SimpleTimer.fromFps(2) {
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

  private def downloadDlinkConfig(): DlinkConfiguration = {
    Try(DcsRemote.getBlocking(s"static-data/dlink-settings")) match {
      case Success(data) => JSON.read[DlinkConfiguration](data)
      case Failure(e) => throw new RuntimeException(s"Could not download data link configuration from dcs-remote", e)
    }
  }

}
