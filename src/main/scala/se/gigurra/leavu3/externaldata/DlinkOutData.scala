package se.gigurra.leavu3.externaldata

import se.gigurra.leavu3.{Configuration, DlinkSettings, Mark, Member}
import se.gigurra.leavu3.util.{RestClient, SimpleTimer}
import se.gigurra.serviceutils.json.JSON
import se.gigurra.serviceutils.twitter.logging.Logging
import se.gigurra.serviceutils.twitter.service.ServiceException

import scala.util.{Failure, Success, Try}

object DlinkOutData extends Logging {

  @volatile private var marks: Map[String, Mark] = Map.empty

  def hasMark(id: String): Boolean = marks.contains(id)

  def addMark(mark: Mark): Unit = {
    marks += mark.id -> mark
  }

  def deleteMark(id: String): Unit = {
    marks -= id
  }

  def startPoller(masterConfig: Configuration, config: DlinkSettings): Unit = {
    if (masterConfig.relayDlink) {

      val client = RestClient(config.host, config.port)

      SimpleTimer.fromFps(config.outFps) {
        val source = ExternalData.gameData
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
}