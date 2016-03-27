package se.gigurra.leavu3.interfaces

import com.twitter.finagle.FailedFastException
import com.twitter.util.{Await, Future}
import se.gigurra.heisenberg.MapDataParser
import se.gigurra.leavu3.datamodel.{Configuration, DlinkConfiguration, DlinkData, Mark, Member}
import se.gigurra.leavu3.util.{DefaultTimer, IdenticalRequestPending, RestClient}
import se.gigurra.serviceutils.json.JSON
import se.gigurra.serviceutils.twitter.logging.Logging
import se.gigurra.serviceutils.twitter.service.ServiceException

import scala.util.{Failure, Success, Try}

/**
  * Created by kjolh on 3/20/2016.
  */
object Dlink extends Logging {

  @volatile var config: DlinkConfiguration = DlinkConfiguration()
  @volatile var dlinkClient: Option[RestClient] = None
  @volatile var nameLookupOk = false
  @volatile var recvOk = false

  def connected: Boolean = nameLookupOk && recvOk

  def start(appCfg: Configuration): Unit = {
    In.start()
    if (appCfg.relayDlink)
      Out.start()
    CfgUpdate.start()
  }

  object CfgUpdate {

    def handleDlinkConfig(newConfig: DlinkConfiguration): Unit = {
      if (newConfig != config || !nameLookupOk) {
        logger.info(s"Updating dlink settings to: \n ${JSON.write(newConfig)}")
        config = newConfig
        In.clear()
        Out.clear()
        dlinkClient = Try(RestClient(newConfig.host, newConfig.port, "Data Link")).toOption
        nameLookupOk = dlinkClient.isDefined
      }
    }

    def start(): Unit = {
      DefaultTimer.seconds(3) {
        downloadDlinkConfig().map(handleDlinkConfig).onFailure {
          case e: IdenticalRequestPending =>
          case e: FailedFastException =>
          case e => logger.warning(s"Unable to update data link configuration: $e")
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

    def clear(): Unit = {
      ownTeam = Map.empty
      allTeams = Map.empty
    }

    def start(): Unit = {

      DefaultTimer.fps(1) {
        dlinkClient.foreach(_.get(config.team, maxAge = Some(10000L)).map { jsonString =>
          val rawData = JSON.readMap(jsonString)
          val everyoneOnNetwork = rawData.collect { case ValidDlinkData(id, dlinkData) => id -> dlinkData }
          allTeams = everyoneOnNetwork.groupBy(_._2.selfData.coalitionId)
          ownTeam = allTeams.getOrElse(GameIn.snapshot.selfData.coalitionId, Map.empty)
          recvOk = true
        }.onFailure {
          case e: IdenticalRequestPending =>
            // Do nothing
          case e: ServiceException =>
            recvOk = false
            allTeams = Map.empty
            ownTeam = Map.empty
            logger.error(s"Data link host replied with an error: $e")
          case e: FailedFastException =>
            recvOk = false
            allTeams = Map.empty
            ownTeam = Map.empty
          // Ignore ..
          case e =>
            recvOk = false
            allTeams = Map.empty
            ownTeam = Map.empty
            logger.error(e, s"Unexpected error when attempting to receive from dlink")
        })
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

    def clear(): Unit = {
      marks = Map.empty
    }

    def start(): Unit = {

      DefaultTimer.fps(2) {

        val source = GameIn.snapshot
        if (source.err.isEmpty && source.age < 3.0) {

          dlinkClient.foreach {
            _.put(s"${config.team}/${config.callsign}") {

              val self = Member.marshal(
                Member.planeId -> source.metaData.planeId,
                Member.modelTime -> source.metaData.modelTime,
                Member.position -> source.selfData.position,
                Member.velocity -> source.flightModel.velocity,
                Member.targets -> source.sensors.targets.locked,
                Member.selfData -> source.metaData.selfData,
                Member.markPos -> marks
              )
              JSON.write(self)
            }.onFailure {
              case e: IdenticalRequestPending => // ignore
              case e: FailedFastException => // ignore
              case e: ServiceException => logger.error(s"Data link host replied with an error: $e")
              case e => logger.error(e, s"Unexpected error when attempting to send to dlink")
            }
          }
        }
      }
    }
  }

  private def downloadDlinkConfig(): Future[DlinkConfiguration] = {
    DcsRemote.get(s"static-data/dlink-settings").map(JSON.read[DlinkConfiguration])
  }

}
