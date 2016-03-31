package se.gigurra.leavu3.datamodel

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Schema, Parsed}

case class Targets(source: SourceData = Map.empty) extends SafeParsed[Targets.type] {
  val detected = parse(schema.detected)
  val tws      = parse(schema.tws)
  val locked   = parse(schema.locked)

  def pdt: Option[Target] = locked.headOption

  def withRwsMemory(rwsContacts: Seq[Contact]): Targets = marshal(this, schema.detected -> rwsContacts)
}

object Targets extends Schema[Targets] {
  val detected = required[Seq[Contact]]("detected", default = Seq.empty)
  val tws      = required[Seq[Target]]("tws", default = Seq.empty)
  val locked   = required[Seq[Target]]("locked", default = Seq.empty)
}

