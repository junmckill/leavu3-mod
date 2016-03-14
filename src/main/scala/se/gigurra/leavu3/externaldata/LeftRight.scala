package se.gigurra.leavu3.externaldata

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Schema, Parsed}

case class LeftRight(source: SourceData = Map.empty) extends SafeParsed[LeftRight.type] {
  val left  = parse(schema.left)
  val right = parse(schema.right)
}

object LeftRight extends Schema[LeftRight] {
  val left  = required[Float]("left", default = 0)
  val right = required[Float]("right", default = 0)
}

