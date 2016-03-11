package se.gigurra.leavu3.externaldata

import com.badlogic.gdx.math.Vector3
import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Parsed, Schema}
import se.gigurra.leavu3.util.{RestClient, SimpleTimer}
import se.gigurra.serviceutils.json.JSON
import scala.language.implicitConversions
import mappers._

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
  val metaData        = parse(schema.metadata)
}

object GameData extends Schema[GameData] {

  // DCS Remote Metadata
  val err       = optional[String]("err")
  val requestId = optional[String]("requestId")

  // Actual game data
  val rwr             = optional[Rwr]("rwr")
  val counterMeasures = optional[CounterMeasures]("counterMeasures")
  val payload         = optional[Payload]("payload")
  val flightModel     = optional[FlightModel]("flightModel")
  val sensors         = optional[Sensors]("sensor")
  val aiWingmenTgts   = optional[Seq[Vector3]]("wingTargets")
  val indicators      = optional[Indicators]("indicators")
  val aiWingmen       = optional[Seq[Option[AiWingman]]]("wingMen")
  val route           = optional[Route]("route")
  val metadata        = optional[MetaData]("metaData")


  ///////////////////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////


  val path = "export/dcs_remote_export_data()"

  def startPoller(fps: Int, addr: String, port: Int): Unit = {

    val client = RestClient(addr, port)

    SimpleTimer.fromFps(fps) {
      val stringData = client.getBlocking(path, cacheMaxAgeMillis = Some((1000.0 / fps / 2.0).toLong))
      val newData = JSON.read(stringData)
      ExternalData.gameData = process(newData)
    }
  }

  private def process(newData: GameData): GameData = {
    // TODO: Fuse with old data where required
    // Known states that needs fusion:
    // - rws detections (only visible a few frames) - Needs manual storage
    // - If in NAV mode: Store waypoints that have not yet been seen (workaround for missing waypoints bug)
    newData
  }

}


