package se.gigurra.leavu3.externaldata

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Schema, Parsed}

case class SensorAngles(source: SourceData = Map.empty) extends SafeParsed[SensorAngles.type] {
  val azimuth   = parse(schema.azimuth).toDegrees
  val elevation = parse(schema.elevation).toDegrees
}

object SensorAngles extends Schema[SensorAngles] {
  val azimuth   = required[Float]("azimuth", default = 0)
  val elevation = required[Float]("elevation", default = 0)
}

