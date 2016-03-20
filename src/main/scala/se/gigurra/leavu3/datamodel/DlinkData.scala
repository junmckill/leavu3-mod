package se.gigurra.leavu3.datamodel

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.Schema
import se.gigurra.leavu3.util.CurTime
import scala.language.implicitConversions

/**
  * Created by kjolh on 3/20/2016.
  */

case class DlinkData(source: SourceData = Map.empty) extends SafeParsed[DlinkData.type] {
  val timestamp = parse(schema.timestamp)
  val age       = parse(schema.age)
  val data      = parse(schema.data)
}

object DlinkData extends Schema[DlinkData] {
  val timestamp = required[Double]("timestamp", default = CurTime.seconds)
  val age = required[Double]("age", default = 0.0)
  val data = required[Member]("data", default = Member())

  implicit def toMember(d: DlinkData): Member = d.data
}