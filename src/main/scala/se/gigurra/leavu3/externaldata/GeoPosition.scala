package se.gigurra.leavu3.externaldata

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Schema, Parsed}

case class GeoPosition(source: SourceData = Map.empty) extends SafeParsed[GeoPosition.type] {
  val lat = parse(schema.lat).toDegrees
  val lon = parse(schema.lon).toDegrees
  val alt = parse(schema.alt).toDegrees
}

object GeoPosition extends Schema[GeoPosition] {
  val lat = required[Double]("Lat", default = 0)
  val lon = required[Double]("Long", default = 0)
  val alt = required[Double]("Alt", default = 0)
}

