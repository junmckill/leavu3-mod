package se.gigurra.leavu3.externaldata

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Schema, Parsed}

case class Prf(source: SourceData = Map.empty) extends Parsed[Prf.type] {
  val selection = parse(schema.selection)
  val current   = parse(schema.current)
}

object Prf extends Schema[Prf] {
  val selection = required[String]("selection", default = "")
  val current   = required[String]("current", default = "")
}

