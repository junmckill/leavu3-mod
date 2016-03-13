package se.gigurra.leavu3


import java.time.Instant

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Schema, Parsed}
import se.gigurra.leavu3.externaldata.{Vec3, Target, SelfData, Version}

case class Member(source: SourceData = Map.empty) extends Parsed[Member.type] {
  val planeId   = parse(schema.planeId)
  val modelTime = parse(schema.modelTime)
  val position  = parse(schema.position)
  val velocity  = parse(schema.velocity)
  val selfData  = parse(schema.selfData)
  val targets   = parse(schema.targets)
}

object Member extends Schema[Member] {
  val planeId   = required[Int]("playerPlaneId", default = 0)
  val modelTime = required[Double]("modelTime", default = 0)
  val position  = required[Vec3]("position", default = Vec3())
  val velocity  = required[Vec3]("velocity", default = Vec3())
  val selfData  = required[SelfData]("selfData", default = SelfData())
  val targets   = required[Seq[Target]]("targets", default = Seq.empty)
}

case class DlinkData(source: SourceData = Map.empty) extends Parsed[DlinkData.type] {
  val timestamp = parse(schema.timestamp)
  val age       = parse(schema.age)
  val data      = parse(schema.data)
}

object DlinkData extends Schema[DlinkData] {
  val timestamp = required[Double]("timestamp", default = Instant.now.toEpochMilli.toDouble / 1000.0)
  val age       = required[Double]("age", default = 0.0)
  val data      = required[Member]("data", default = Member())
}
