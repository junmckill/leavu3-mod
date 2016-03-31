package se.gigurra.leavu3.datamodel

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Schema, Parsed}

case class RadarDisplayScale(source: SourceData = Map.empty) extends SafeParsed[RadarDisplayScale.type] {
  val azimuth  = math.max(parse(schema.azimuth).toDegrees * 2.0, 0.1)
  val distance = math.max(parse(schema.distance), 10)
}

object RadarDisplayScale extends Schema[RadarDisplayScale] {
  val azimuth  = required[Float]("azimuth", default = 0)
  val distance = required[Float]("distance", default = 0)
}

