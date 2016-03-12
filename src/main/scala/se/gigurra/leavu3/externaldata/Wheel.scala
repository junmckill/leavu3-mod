package se.gigurra.leavu3.externaldata

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Schema, Parsed}

case class Wheel(source: SourceData = Map.empty) extends Parsed[Wheel.type] {
  val rod = parse(schema.rod)
}

object Wheel extends Schema[Wheel] {
  val rod = required[Float]("rod", default = 0)
}
