package se.gigurra.leavu3.externaldata

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Schema, Parsed}

case class AiWingman(source: SourceData) extends Parsed[AiWingman.type] {
  val orderedTask     = parse(schema.orderedTask)
  val currentTarget   = parse(schema.currentTarget)
  val currentTask     = parse(schema.currentTask)
  val spatial         = parse(schema.wingmenPosition)
  val id              = parse(schema.wingmenId)
  val orderedTarget   = parse(schema.orderedTarget)

  def position = spatial.position
  def pitch = spatial.pitch
  def roll = spatial.roll
  def heading = spatial.heading
}

object AiWingman extends Schema[AiWingman] {
  val orderedTask     = required[String]("ordered_task")
  val currentTarget   = required[Int]("current_target")
  val currentTask     = required[String]("current_task")
  val wingmenPosition = required[Spatial]("wingmen_position")
  val wingmenId       = required[Int]("wingmen_id")
  val orderedTarget   = required[Int]("ordered_target")
}

