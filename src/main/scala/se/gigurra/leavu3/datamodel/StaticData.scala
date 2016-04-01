package se.gigurra.leavu3.datamodel

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Parsed, Schema}

case class StaticData(source: SourceData = Map.empty) extends Parsed[StaticData.type] {
  val dlinkSettings = parse(schema.dlinkSettings)
  val missionSettings = parse(schema.missionSettings)
}

object StaticData extends Schema[StaticData] {
  val dlinkSettings = required[DlinkConfiguration]("dlink-settings", default = DlinkConfiguration())
  val missionSettings = required[MissionConfiguration]("mission-settings", default = MissionConfiguration())
}
