package se.gigurra.leavu3.externaldata

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Schema, Parsed}

case class Spatial(source: SourceData = Map.empty) extends Parsed[Spatial.type] {
  val rwing    = parse(schema.x)
  val nose     = parse(schema.y)
  val up       = parse(schema.z)
  val position = parse(schema.position)
}

object Spatial extends Schema[Spatial] {
  val x        = required[Vec3]("x", default = Vec3())
  val y        = required[Vec3]("y", default = Vec3())
  val z        = required[Vec3]("z", default = Vec3())
  val position = required[Vec3]("p", default = Vec3())
}

