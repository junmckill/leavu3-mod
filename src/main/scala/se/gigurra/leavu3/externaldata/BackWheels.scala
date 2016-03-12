package se.gigurra.leavu3.externaldata

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Schema, Parsed}

case class BackWheels(source: SourceData = Map.empty) extends Parsed[BackWheels.type] {
  val left  = parse(schema.left)
  val right = parse(schema.right)
}

object BackWheels extends Schema[BackWheels] {
  val left  = required[Wheel]("left", default = Wheel())
  val right = required[Wheel]("right", default = Wheel())
}


