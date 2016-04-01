package se.gigurra.leavu3.datamodel

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Parsed, Schema}

case class DcsRemoteRemoteConfig(source: SourceData = Map.empty) extends Parsed[DcsRemoteRemoteConfig.type] {
  val dlinkSettings = parse(schema.dlinkSettings)
  val missionSettings = parse(schema.missionSettings)
}

object DcsRemoteRemoteConfig extends Schema[DcsRemoteRemoteConfig] {
  val dlinkSettings = required[DlinkConfiguration]("dlink-settings", default = DlinkConfiguration())
  val missionSettings = required[MissionConfiguration]("mission-settings", default = MissionConfiguration())
}
