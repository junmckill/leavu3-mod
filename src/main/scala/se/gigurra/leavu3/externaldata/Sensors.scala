package se.gigurra.leavu3.externaldata

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Schema, Parsed}

case class Sensors(source: SourceData = Map.empty) extends Parsed[Sensors.type] {
  val status = parse(schema.status)
  val targets = parse(schema.targets)

  def pdt: Option[Target] = targets.pdt
}

object Sensors extends Schema[Sensors] {
  val status  = required[SensorsStatus]("status", default = SensorsStatus())
  val targets = required[Targets]("targets", default = Targets())
}

