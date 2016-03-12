package se.gigurra.leavu3.externaldata

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Schema, Parsed}

case class UnitType(source: SourceData = Map.empty) extends Parsed[UnitType.type] {
  val level1 = parse(schema.level1)
  val level2 = parse(schema.level2)
  val level3 = parse(schema.level3)
  val level4 = parse(schema.level4)
}

object UnitType extends Schema[UnitType] {
  val level1 = required[Int]("level1", default = 0)
  val level2 = required[Int]("level2", default = 0)
  val level3 = required[Int]("level3", default = 0)
  val level4 = required[Int]("level4", default = 0)
}
