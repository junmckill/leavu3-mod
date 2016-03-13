package se.gigurra.leavu3.externaldata

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Schema, Parsed}

case class Targets(source: SourceData = Map.empty) extends Parsed[Targets.type] {
  val detected = parse(schema.detected)
  val tws      = parse(schema.tws)
  val locked   = parse(schema.locked)

  def pdt: Option[Target] = locked.headOption
}

object Targets extends Schema[Targets] {
  val detected = required[Seq[Contact]]("detected", default = Seq.empty)
  val tws      = required[Seq[Target]]("tws", default = Seq.empty)
  val locked   = required[Seq[Target]]("locked", default = Seq.empty)
}

