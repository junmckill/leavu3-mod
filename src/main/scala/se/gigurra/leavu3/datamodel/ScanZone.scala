package se.gigurra.leavu3.datamodel

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Schema, Parsed}

case class ScanZone(source: SourceData = Map.empty) extends SafeParsed[ScanZone.type] {
  val altitudeCoverage = parse(schema.altitudeCoverage)
  val size             = parse(schema.size)
  val direction        = parse(schema.direction)
}

object ScanZone extends Schema[ScanZone] {
  val altitudeCoverage = required[MinMax]("coverage_H", default = MinMax())
  val size             = required[SensorAngles]("size", default = SensorAngles())
  val direction        = required[SensorAngles]("position", default = SensorAngles())
}
