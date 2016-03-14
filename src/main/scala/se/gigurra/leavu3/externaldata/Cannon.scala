package se.gigurra.leavu3.externaldata

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Schema, Parsed}

case class Cannon(source: SourceData = Map.empty) extends SafeParsed[Cannon.type] {
  val shells = parse(schema.shells)
}

object Cannon extends Schema[Cannon] {
  val shells = required[Int]("shells", default = 0)
}

