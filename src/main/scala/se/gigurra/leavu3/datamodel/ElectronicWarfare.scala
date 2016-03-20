package se.gigurra.leavu3.datamodel

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.Schema

case class ElectronicWarfare(source: SourceData = Map.empty) extends SafeParsed[ElectronicWarfare.type] {
  val rwr             = parse(schema.rwr)
  val counterMeasures = parse(schema.counterMeasures)
}

object ElectronicWarfare extends Schema[ElectronicWarfare] {
  val rwr             = required[Rwr]("rwr", default = Rwr())
  val counterMeasures = required[CounterMeasures]("counterMeasures", default = CounterMeasures())
}
