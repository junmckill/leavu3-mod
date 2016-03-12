package se.gigurra.leavu3.externaldata

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Schema, Parsed}

case class RadarDisplayScale(source: SourceData = Map.empty) extends Parsed[RadarDisplayScale.type] {
  val azimuth  = parse(schema.azimuth).toDegrees
  val distance = parse(schema.distance)
}

object RadarDisplayScale extends Schema[RadarDisplayScale] {
  val azimuth  = required[Float]("azimuth", default = 0)
  val distance = required[Float]("distance", default = 0)
}

