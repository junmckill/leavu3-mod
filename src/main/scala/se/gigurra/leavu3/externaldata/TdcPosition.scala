package se.gigurra.leavu3.externaldata

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Schema, Parsed}

case class TdcPosition(source: SourceData = Map.empty) extends SafeParsed[TdcPosition.type] {
  val x = parse(schema.x)
  val y = parse(schema.y)
}

object TdcPosition extends Schema[TdcPosition] {
  val x = required[Float]("x", default = 0)
  val y = required[Float]("y", default = 0)
}

