package se.gigurra.leavu3.externaldata

import com.badlogic.gdx.math.Vector3
import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{MapDataProducer, MapDataParser, Schema, Parsed}
import se.gigurra.leavu3.util.{RestClient, SimpleTimer}
import se.gigurra.serviceutils.json.JSON
import mappers._

object mappers {

  implicit val vec3MapDataParser = new MapDataParser[Vector3] {
    override def parse(field: Any): Vector3 = {
      val data = field.asInstanceOf[Map[String, Number]]
      new Vector3(data("x").floatValue, data("y").floatValue, data.get("z").map(_.floatValue).getOrElse(0.0f))
    }
  }

  implicit val vec3MapDataProducer = new MapDataProducer[Vector3] {
    override def produce(t: Vector3): Any = {
      Map("x" -> t.x, "y" -> t.y, "z" -> t.z)
    }
  }
}

case class UnitType(source: SourceData) extends Parsed[UnitType.type] {
  val level1          = parse(schema.level1)
  val level2          = parse(schema.level2)
  val level3          = parse(schema.level3)
  val level4          = parse(schema.level4)
}

object UnitType extends Schema[UnitType] {
  val level1          = required[Int]("level1")
  val level2          = required[Int]("level2")
  val level3          = required[Int]("level3")
  val level4          = required[Int]("level4")
}

case class Emitter(source: SourceData) extends Parsed[Emitter.type] {
  val id          = parse(schema.id)
  val signalType  = parse(schema.signalType)
  val azimuth     = parse(schema.azimuth)
  val priority    = parse(schema.priority)
  val power       = parse(schema.power)
  val unitType    = parse(schema.unitType)
}

object Emitter extends Schema[Emitter] {
  val id          = required[Int]("ID")
  val signalType  = required[String]("SignalType")
  val azimuth     = required[Double]("Azimuth")
  val priority    = required[Double]("Priority")
  val power       = required[Double]("Power")
  val unitType    = required[UnitType]("Type")
}

case class Rwr(source: SourceData) extends Parsed[Rwr.type] {
  val emitters = parse(schema.emitters)
  val mode     = parse(schema.mode)
}

object Rwr extends Schema[Rwr] {
  val emitters = required[Seq[Emitter]]("Emitters")
  val mode     = required[Int]("Mode")
}

case class CounterMeasures(source: SourceData) extends Parsed[CounterMeasures.type] {
  val chaff = parse(schema.chaff)
  val flare = parse(schema.flare)
}

object CounterMeasures extends Schema[CounterMeasures] {
  val chaff = required[Int]("chaff")
  val flare = required[Int]("flare")
}

case class PayloadStation(source: SourceData) extends Parsed[PayloadStation.type] {
  val clsid       = parse(schema.clsid)
  val isContainer = parse(schema.isContainer)
  val count       = parse(schema.count)
  val typ         = parse(schema.typ)
}

object PayloadStation extends Schema[PayloadStation] {
  val clsid       = required[String]("CLSID")
  val isContainer = required[Boolean]("container")
  val count       = required[Int]("count")
  val typ         = required[UnitType]("weapon")
}

case class Cannon(source: SourceData) extends Parsed[Cannon.type] {
  val shells = parse(schema.shells)
}

object Cannon extends Schema[Cannon] {
  val shells = required[Int]("shells")
}

case class Payload(source: SourceData) extends Parsed[Payload.type] {
  val stations       = parse(schema.stations)
  val currentStation = parse(schema.currentStation)
  val cannon         = parse(schema.cannon)
}

object Payload extends Schema[Payload] {
  val stations       = required[Seq[PayloadStation]]("Stations")
  val currentStation = required[Int]("CurrentStation")
  val cannon         = required[Cannon]("Cannon")
}

case class FlightModel(source: SourceData) extends Parsed[FlightModel.type] {
  val pitch             = parse(schema.pitch)
  val roll              = parse(schema.roll)
  val trueHeading       = parse(schema.trueHeading)
  val magneticHeading   = parse(schema.magneticHeading)
  val angleOfAttack     = parse(schema.angleOfAttack)

  val velocity          = parse(schema.velocity)
  val acceleration      = parse(schema.acceleration)

  val indicatedAirspeed = parse(schema.indicatedAirspeed)
  val trueAirspeed      = parse(schema.trueAirspeed)
  val verticalVelocity  = parse(schema.verticalVelocity)
  val machNumber        = parse(schema.machNumber)

  val altitudeAGL       = parse(schema.altitudeAGL)
  val altitudeAsl       = parse(schema.altitudeAsl)

  val windVelocity      = parse(schema.windVelocity)
  val airPressure       = parse(schema.airPressure)
  val slipBallDeviation = parse(schema.slipBallDeviation)

  val ilsLocalizer      = parse(schema.ilsLocalizer)
  val ilsGlideslope     = parse(schema.ilsGlideslope)
}

object FlightModel extends Schema[FlightModel] {
  val pitch             = required[Double]("pitch")
  val roll              = required[Double]("roll")
  val trueHeading       = required[Double]("heading")
  val magneticHeading   = required[Double]("magneticYaw")
  val angleOfAttack     = required[Double]("AOA")

  val velocity          = required[Vector3]("vectorVelocity")
  val acceleration      = required[Vector3]("acc")

  val indicatedAirspeed = required[Double]("IAS")
  val trueAirspeed      = required[Double]("TAS")
  val verticalVelocity  = required[Double]("vv")
  val machNumber        = required[Double]("mach")

  val altitudeAGL       = required[Double]("altitudeAboveGroundLevel")
  val altitudeAsl       = required[Double]("altitudeAboveSeaLevel")

  val windVelocity      = required[Vector3]("windVectorVelocity")
  val airPressure       = required[Double]("atmospherePressure")
  val slipBallDeviation = required[Double]("slipBallPosition")

  val ilsLocalizer      = required[Double]("sideDeviation")
  val ilsGlideslope     = required[Double]("glideDeviation")
}

case class SensorAngles(source: SourceData) extends Parsed[SensorAngles.type] {
  val azimuth   = parse(schema.azimuth)
  val elevation = parse(schema.elevation)
}

object SensorAngles extends Schema[SensorAngles] {
  val azimuth   = required[Double]("azimuth")
  val elevation = required[Double]("elevation")
}

case class RadarDisplayScale(source: SourceData) extends Parsed[RadarDisplayScale.type] {
  val azimuth  = parse(schema.azimuth)
  val distance = parse(schema.distance)
}

object RadarDisplayScale extends Schema[RadarDisplayScale] {
  val azimuth  = required[Double]("azimuth")
  val distance = required[Double]("distance")
}

case class MinMax(source: SourceData) extends Parsed[MinMax.type] {
  val min = parse(schema.min)
  val max = parse(schema.max)
}

object MinMax extends Schema[MinMax] {
  val min = required[Double]("min")
  val max = required[Double]("max")
}

case class SensorsStatus(source: SourceData) extends Parsed[SensorsStatus.type] {
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
  val manufacturer      = required[String]("Manufacturer")
  val launchAuthorized  = required[Boolean]("LaunchAuthorized")
  val eosOn             = required[Boolean]("optical_system_on")
  val radarOn           = required[Boolean]("radar_on")
  val laserOn           = required[Boolean]("laser_on")
  val ecmOn             = required[Boolean]("ECM_on")
  val scale             = required[RadarDisplayScale]("scale")
  val tdc               = required[Vector3]("TDC")
  val scanZone          = required[ScanZone]("ScanZone")
  val prf               = required[Prf]("PRF")
}

case class ScanZone(source: SourceData) extends Parsed[ScanZone.type] {
  val altitudeCoverage = parse(schema.altitudeCoverage)
  val size             = parse(schema.size)
  val direction        = parse(schema.direction)
}

object ScanZone extends Schema[ScanZone] {
  val altitudeCoverage = required[MinMax]("coverage_H")
  val size             = required[SensorAngles]("size")
  val direction        = required[SensorAngles]("position")
}

case class Prf(source: SourceData) extends Parsed[Prf.type] {
  val selection = parse(schema.selection)
  val current   = parse(schema.current)
}

object Prf extends Schema[Prf] {
  val selection = required[String]("selection")
  val current   = required[String]("current")
}

case class DetectedTarget(source: SourceData) extends Parsed[DetectedTarget.type] {
}

object DetectedTarget extends Schema[DetectedTarget] {
}

case class TwsTarget(source: SourceData) extends Parsed[TwsTarget.type] {
}

object TwsTarget extends Schema[TwsTarget] {
}

case class LockedTarget(source: SourceData) extends Parsed[LockedTarget.type] {
}

object LockedTarget extends Schema[LockedTarget] {
}

case class Targets(source: SourceData) extends Parsed[Targets.type] {
  val detected = parse(schema.detected)
  val tws      = parse(schema.tws)
  val locked   = parse(schema.locked)
}

object Targets extends Schema[Targets] {
  val detected = required[Seq[DetectedTarget]]("detected")
  val tws      = required[Seq[TwsTarget]]("tws")
  val locked   = required[Seq[LockedTarget]]("locked")
}

case class Sensors(source: SourceData) extends Parsed[Sensors.type] {
  val status = parse(schema.status)
  val targets = parse(schema.targets)
}

object Sensors extends Schema[Sensors] {
  val status  = required[SensorsStatus]("status")
  val targets = required[Targets]("targets")
}

case class GameData(source: SourceData) extends Parsed[GameData.type] {

  // DCS Remote Metadata
  val err       = parse(schema.err)
  val requestId = parse(schema.requestId)

  // Actual game data
  val rwr             = parse(schema.rwr)
  val counterMeasures = parse(schema.counterMeasures)
  val payload         = parse(schema.payload)
  val flightModel     = parse(schema.flightModel)
  val sensors         = parse(schema.sensors)
  val aiWingmenTgts   = parse(schema.aiWingmenTgts)
}

object GameData extends Schema[GameData] {

  // DCS Remote Metadata
  val err       = optional[String]("err")
  val requestId = optional[String]("requestId")

  // Actual game data
  val rwr             = optional[Rwr]("rwr")
  val counterMeasures = optional[CounterMeasures]("counterMeasures")
  val payload         = optional[Payload]("payload")
  val flightModel     = optional[FlightModel]("flightModel")
  val sensors         = optional[Sensors]("sensor")
  val aiWingmenTgts   = optional[Seq[Vector3]]("wingTargets")
  //val indicators      = optional[Indicators]("indicators")
  //val aiWingmen       = optional[Seq[Option[AiWingman]]]("wingMen")
  //val route           = optional[Route]("route")
  //val metadata        = optional[Route]("metaData")


  ///////////////////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////


  val path = "export/dcs_remote_export_data()"

  def startPoller(fps: Int, addr: String, port: Int): Unit = {

    val client = RestClient(addr, port)

    SimpleTimer.fromFps(fps) {
      ExternalData.gameData = JSON.read(client.getBlocking(path, cacheMaxAgeMillis = Some((1000.0 / fps / 2.0).toLong)))
      println(ExternalData.gameData.requestId)
    }
  }

}


