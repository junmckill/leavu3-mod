package se.gigurra.leavu3.datamodel

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Schema, Parsed}

case class ControlSurfaces(source: SourceData = Map.empty) extends SafeParsed[ControlSurfaces.type] {
  val aileron  = parse(schema.aileron)
  val elevator = parse(schema.elevator)
  val rudder   = parse(schema.rudder)
}

object ControlSurfaces extends Schema[ControlSurfaces] {
  val aileron  = required[LeftRight]("eleron", default = LeftRight())
  val elevator = required[LeftRight]("elevator", default = LeftRight())
  val rudder   = required[LeftRight]("rudder", default = LeftRight())
}

