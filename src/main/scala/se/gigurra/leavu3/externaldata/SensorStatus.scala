package se.gigurra.leavu3.externaldata

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Schema, Parsed}

case class SensorsStatus(source: SourceData = Map.empty) extends Parsed[SensorsStatus.type] {
  val manufacturer     = parse(schema.manufacturer)
  val launchAuthorized = parse(schema.launchAuthorized)
  val eosOn            = parse(schema.eosOn)
  val radarOn          = parse(schema.radarOn)
  val laserOn          = parse(schema.laserOn)
  val ecmOn            = parse(schema.ecmOn)
  val scale            = parse(schema.scale)
  val tdc              = parse(schema.tdc)
  val scanZone         = parse(schema.scanZone)
  val prf              = parse(schema.prf)
}

object SensorsStatus extends Schema[SensorsStatus] {
  val manufacturer      = required[String]("Manufacturer", default = "")
  val launchAuthorized  = required[Boolean]("LaunchAuthorized", default = false)
  val eosOn             = required[Boolean]("optical_system_on", default = false)
  val radarOn           = required[Boolean]("radar_on", default = false)
  val laserOn           = required[Boolean]("laser_on", default = false)
  val ecmOn             = required[Boolean]("ECM_on", default = false)
  val scale             = required[RadarDisplayScale]("scale", default = RadarDisplayScale())
  val tdc               = required[TdcPosition]("TDC", default = TdcPosition())
  val scanZone          = required[ScanZone]("ScanZone", default = ScanZone())
  val prf               = required[Prf]("PRF", default = Prf())
}