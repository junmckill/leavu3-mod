package se.gigurra.leavu3.externaldata

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Schema, Parsed}

case class CounterMeasures(source: SourceData = Map.empty) extends SafeParsed[CounterMeasures.type] {
  val chaff = parse(schema.chaff)
  val flare = parse(schema.flare)
}

object CounterMeasures extends Schema[CounterMeasures] {
  val chaff = required[Int]("chaff", default = 0)
  val flare = required[Int]("flare", default = 0)
}
