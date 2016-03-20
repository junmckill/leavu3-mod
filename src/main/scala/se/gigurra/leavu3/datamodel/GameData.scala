package se.gigurra.leavu3.datamodel

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.Schema
import se.gigurra.leavu3.util.CurTime
import se.gigurra.serviceutils.twitter.logging.Logging
import scala.language.implicitConversions

case class GameData(source: SourceData = Map.empty) extends SafeParsed[GameData.type] {

  // DCS Remote Metadata
  val err       = parse(schema.err)
  val requestId = parse(schema.requestId)

  // Actual game data
  val electronicWarf  = parse(schema.electronicWarf)
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

  def tdcPosition: Option[Vec2] = sensors.status.tdcPosition(selfData.heading, selfData.position)
  def pdt: Option[Target] = sensors.pdt

  def aircraftMode: AircraftMode = indicators.nav.mode

  def withRoute(newRoute: Route) = marshal(this, schema.route -> newRoute)

  val timeStamp: Double = CurTime.seconds
  def age: Double = CurTime.seconds - timeStamp

  def hasRequestId: Boolean = requestId.isDefined
  def timedOut: Boolean = age > 3.0
  def hasError: Boolean = err.isDefined

  def isValid: Boolean = hasRequestId && !hasError && !timedOut
}

object GameData extends Schema[GameData] with Logging {

  // DCS Remote Metadata
  val err       = optional[String]("err")
  val requestId = optional[String]("requestId")

  // Actual game data
  val electronicWarf  = required[ElectronicWarfare]("electronicWarfare", default = ElectronicWarfare())
  val payload         = required[Payload]("payload", default = Payload())
  val flightModel     = required[FlightModel]("flightModel", default = FlightModel())
  val sensors         = required[Sensors]("sensor", default = Sensors())
  val aiWingmenTgts   = required[Seq[Vec3]]("wingTargets", default = Seq.empty)
  val indicators      = required[Indicators]("indicators", default = Indicators())
  val aiWingmen       = required[Seq[Option[AiWingman]]]("wingMen", default = Seq.empty)
  val route           = required[Route]("route", default = Route())
  val metadata        = required[MetaData]("metaData", default = MetaData())
}


