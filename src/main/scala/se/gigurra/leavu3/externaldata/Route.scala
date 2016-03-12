package se.gigurra.leavu3.externaldata

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Schema, Parsed}

case class Route(source: SourceData = Map.empty) extends Parsed[Route.type] {
  val waypoints       = parse(schema.waypoints)
  val currentWaypoint = parse(schema.currentWaypoint)
}

object Route extends Schema[Route] {
  val waypoints       = required[Seq[Waypoint]]("route", default = Seq.empty)
  val currentWaypoint = required[Waypoint]("goto_point", default = Waypoint())
}

