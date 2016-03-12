package se.gigurra.leavu3.externaldata

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Parsed, Schema}
import se.gigurra.leavu3.util.{RestClient, SimpleTimer}
import se.gigurra.serviceutils.json.JSON
import se.gigurra.serviceutils.twitter.logging.Logging
import se.gigurra.serviceutils.twitter.service.ServiceException
import scala.collection.mutable
import scala.util.Failure
import scala.language.implicitConversions

import scala.util.{Success, Try}

case class GameData(source: SourceData) extends Parsed[GameData.type] {

  // DCS Remote Metadata
  val err       = parse(schema.err)
  val requestId = parse(schema.requestId)

  // Actual game data
  val rwr             = parse(schema.rwr)
  val counterMeasures = parse(schema.counterMeasures)
  val payload         = parse(schema.payload)
  val flightModel     = parse(schema.flightModel)
  val sensors         = parse(schema.sensors)
  val aiWingmenTgts   = parse(schema.aiWingmenTgts)
  val indicators      = parse(schema.indicators)
  val aiWingmen       = parse(schema.aiWingmen).collect {
    case Some(w: AiWingman) => w
  }
  val route           = parse(schema.route)
  val metaData        = parse(schema.metadata)

  def selfData: SelfData = metaData.selfData

  def withRoute(newRoute: Route) = marshal(this, schema.route -> newRoute)
}

object GameData extends Schema[GameData] with Logging {

  // DCS Remote Metadata
  val err       = optional[String]("err")
  val requestId = optional[String]("requestId")

  // Actual game data
  val rwr             = required[Rwr]("rwr", default = Rwr())
  val counterMeasures = required[CounterMeasures]("counterMeasures", default = CounterMeasures())
  val payload         = required[Payload]("payload", default = Payload())
  val flightModel     = required[FlightModel]("flightModel", default = FlightModel())
  val sensors         = required[Sensors]("sensor", default = Sensors())
  val aiWingmenTgts   = required[Seq[Vec3]]("wingTargets", default = Seq.empty)
  val indicators      = required[Indicators]("indicators", default = Indicators())
  val aiWingmen       = required[Seq[Option[AiWingman]]]("wingMen", default = Seq.empty)
  val route           = required[Route]("route", default = Route())
  val metadata        = required[MetaData]("metaData", default = MetaData())


  ///////////////////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////


  val path = "export/dcs_remote_export_data()"

  def startPoller(fps: Int, addr: String, port: Int): Unit = {

    val client = RestClient(addr, port)

    SimpleTimer.fromFps(fps) {
      Try(client.getBlocking(path, cacheMaxAgeMillis = Some((1000.0 / fps / 2.0).toLong))) match {
        case Success(stringData) =>
          val newData = JSON.read(stringData)
          ExternalData.gameData = process(newData)
        case Failure(e: ServiceException) => logger.warning(s"Dcs Remote replied: Could not fetch game data from Dcs Remote: $e")
        case Failure(e) => logger.error(s"Could not fetch game data from Dcs Remote: $e", e)
      }
    }
  }

  private val knownNavWaypoints = new mutable.HashMap[Int, Waypoint]

  private def process(newData: GameData): GameData = {
    // TODO: Fuse with old data where required
    // Known states that needs fusion:
    // - rws detections (only visible a few frames) - Needs manual storage
    // - If in NAV mode: Store waypoints that have not yet been seen (workaround for missing waypoints bug)
    newData.waypointWorkaround()
  }

  implicit class GameDataWorkarounds(newData: GameData) {

    def waypointWorkaround(): GameData = {
      if (newData.route.waypoints.nonEmpty) {
        newData
      } else {
        // DCS FC waypoint export in NAV mode is broken.. we only get the current one.
        // so keep in memory which ones we've cycled through (yay - Nice idea by GG!)
        val curWp = newData.route.currentWaypoint
        knownNavWaypoints.get(curWp.index) match {
          case Some(knownWaypoint) if knownWaypoint != curWp =>
            logger.info(s"Known navpoint changed - assuming mission change - clearing known navpoints!")
            knownNavWaypoints.clear()
          case _ =>
          // No clearing required. Same or new waypoint
        }

        // DCS gives us an extra wp (index 2 million :))
        if (0 <= curWp.index && curWp.index < 100)
          knownNavWaypoints.put(curWp.index, curWp)
        else
          knownNavWaypoints.put(-1, curWp.withIndex(-1))

        val allWps = knownNavWaypoints.values.toSeq.sortBy(_.index)
        val newRoute = newData.route.withWaypoints(allWps)
        newData.withRoute(newRoute)
      }
    }

  }
}


