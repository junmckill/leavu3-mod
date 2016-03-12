package se.gigurra.leavu3.externaldata

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Schema, Parsed}

case class MetaData(source: SourceData = Map.empty) extends Parsed[MetaData.type] {
  val camera   = parse(schema.camera)
  val planeId  = parse(schema.planeId)
  val time     = parse(schema.time)
  val version  = parse(schema.version)
  val selfData = parse(schema.selfData)
}

object MetaData extends Schema[MetaData] {
  val camera   = required[Spatial]("camPos", default = Spatial())
  val planeId  = required[Int]("playerPlaneId", default = 0)
  val time     = required[Double]("modelTime", default = 0)
  val version  = required[Version]("version", default = Version())
  val selfData = required[SelfData]("selfData", default = SelfData())
}