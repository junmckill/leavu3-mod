package se.gigurra.leavu3.externaldata

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Schema, Parsed}

case class Emitter(source: SourceData) extends SafeParsed[Emitter.type] {
  val id          = parse(schema.id)
  val signalType  = parse(schema.signalType)
  val azimuth     = parse(schema.azimuth).toDegrees
  val priority    = parse(schema.priority)
  val power       = parse(schema.power)
  val typ         = parse(schema.unitType)

  def isSearch = signalType == Emitter.RADAR_SEARCH || signalType == Emitter.RADAR_TWS
}

object Emitter extends Schema[Emitter] {
  val id          = required[Int]("ID")
  val signalType  = required[String]("SignalType")
  val azimuth     = required[Float]("Azimuth")
  val priority    = required[Float]("Priority")
  val power       = required[Float]("Power")
  val unitType    = required[UnitType]("Type")

  val RADAR_SEARCH = "scan"
  val RADAR_TWS = "track_while_scan"
  val RADAR_LOCK = "lock"
  val MISSILE_LAUNCH = "missile_radio_guided"
  val MISSILE_ACTIVE = "missile_active_homing"
}
