package se.gigurra.leavu3.datamodel

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Parsed, Schema}
import se.gigurra.leavu3.lmath.UnitConversions

case class RadarDisplayScale(source: SourceData = Map.empty)
  extends SafeParsed[RadarDisplayScale.type]
  with UnitConversions {
  val azimuth  = math.max(parse(schema.azimuth).toDegrees * 2.0, 0.1)
  val distance = math.max(parse(schema.distance), 80 * nmi_to_m)
}

object RadarDisplayScale extends Schema[RadarDisplayScale] {
  val azimuth  = required[Float]("azimuth", default = 0)
  val distance = required[Float]("distance", default = 0)
}

