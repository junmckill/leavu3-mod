package se.gigurra.leavu3.datamodel

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Schema, Parsed}

case class Waypoint(source: SourceData = Map.empty) extends SafeParsed[Waypoint.type] {
  val position      = parse(schema.position)
  val estimatedTime = parse(schema.estimatedTime)
  val speedReq      = parse(schema.speedReq)
  val pointAction   = parse(schema.pointAction)
  val index         = parse(schema.index)
  val next          = parse(schema.next)

  def withIndex(i: Int) = marshal(this, schema.index -> i)
}

object Waypoint extends Schema[Waypoint] {
  val position      = required[Vec3]("world_point", default = Vec3())
  val estimatedTime = required[Double]("estimated_time", default = 0)
  val speedReq      = required[Double]("speed_req", default = 0)
  val pointAction   = required[String]("point_action", default = "")
  val index         = required[Int]("this_point_num", default = 0)
  val next          = required[Int]("next_point_num", default = 0)
}

