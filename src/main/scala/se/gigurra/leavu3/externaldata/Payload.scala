package se.gigurra.leavu3.externaldata

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Schema, Parsed}

case class Payload(source: SourceData = Map.empty) extends SafeParsed[Payload.type] {
  val stations       = parse(schema.stations)
  val currentStation = parse(schema.currentStation)
  val cannon         = parse(schema.cannon)
}

object Payload extends Schema[Payload] {
  val stations       = required[Seq[PayloadStation]]("Stations", default = Seq.empty)
  val currentStation = required[Int]("CurrentStation", default = 0)
  val cannon         = required[Cannon]("Cannon", default = Cannon())
}
