package se.gigurra.leavu3.externaldata

import com.badlogic.gdx.math.Vector3
import mappers._
import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Schema, Parsed, MapDataProducer, MapDataParser}
import scala.language.implicitConversions

object mappers {

  implicit val vec3MapDataParser = new MapDataParser[Vector3] {
    override def parse(field: Any): Vector3 = {
      val data = field.asInstanceOf[Map[String, Number]]
      val dcsX_North = data("x").floatValue
      val dcsY_Up = data("y").floatValue
      val dcsZ_East = data("z").floatValue
      new Vector3(dcsZ_East, dcsX_North, dcsY_Up)
    }
  }

  implicit val vec3MapDataProducer = new MapDataProducer[Vector3] {
    override def produce(t: Vector3): Any = {
      val dcsX_North = t.y
      val dcsY_Up = t.z
      val dcsZ_East = t.x
      Map("x" -> dcsX_North, "y" -> dcsY_Up, "z" -> dcsZ_East)
    }
  }
}

case class UnitType(source: SourceData = Map.empty) extends Parsed[UnitType.type] {
  val level1 = parse(schema.level1)
  val level2 = parse(schema.level2)
  val level3 = parse(schema.level3)
  val level4 = parse(schema.level4)
}

object UnitType extends Schema[UnitType] {
  val level1 = required[Int]("level1", default = 0)
  val level2 = required[Int]("level2", default = 0)
  val level3 = required[Int]("level3", default = 0)
  val level4 = required[Int]("level4", default = 0)
}

case class Emitter(source: SourceData) extends Parsed[Emitter.type] {
  val id          = parse(schema.id)
  val signalType  = parse(schema.signalType)
  val azimuth     = parse(schema.azimuth).toDegrees
  val priority    = parse(schema.priority)
  val power       = parse(schema.power)
  val unitType    = parse(schema.unitType)
}

object Emitter extends Schema[Emitter] {
  val id          = required[Int]("ID")
  val signalType  = required[String]("SignalType")
  val azimuth     = required[Float]("Azimuth")
  val priority    = required[Float]("Priority")
  val power       = required[Float]("Power")
  val unitType    = required[UnitType]("Type")
}

case class Rwr(source: SourceData = Map.empty) extends Parsed[Rwr.type] {
  val emitters = parse(schema.emitters)
  val mode     = parse(schema.mode)
}

object Rwr extends Schema[Rwr] {
  val emitters = required[Seq[Emitter]]("Emitters", default = Seq.empty)
  val mode     = required[Int]("Mode", default = 0)
}

case class CounterMeasures(source: SourceData = Map.empty) extends Parsed[CounterMeasures.type] {
  val chaff = parse(schema.chaff)
  val flare = parse(schema.flare)
}

object CounterMeasures extends Schema[CounterMeasures] {
  val chaff = required[Int]("chaff", default = 0)
  val flare = required[Int]("flare", default = 0)
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

case class Cannon(source: SourceData = Map.empty) extends Parsed[Cannon.type] {
  val shells = parse(schema.shells)
}

object Cannon extends Schema[Cannon] {
  val shells = required[Int]("shells", default = 0)
}

case class Payload(source: SourceData = Map.empty) extends Parsed[Payload.type] {
  val stations       = parse(schema.stations)
  val currentStation = parse(schema.currentStation)
  val cannon         = parse(schema.cannon)
}

object Payload extends Schema[Payload] {
  val stations       = required[Seq[PayloadStation]]("Stations", default = Seq.empty)
  val currentStation = required[Int]("CurrentStation", default = 0)
  val cannon         = required[Cannon]("Cannon", default = Cannon())
}

case class FlightModel(source: SourceData = Map.empty) extends Parsed[FlightModel.type] {
  val pitch             = parse(schema.pitch).toDegrees
  val roll              = parse(schema.roll).toDegrees
  val trueHeading       = parse(schema.trueHeading).toDegrees
  val magneticHeading   = parse(schema.magneticHeading).toDegrees
  val angleOfAttack     = parse(schema.angleOfAttack).toDegrees

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
  val pitch             = required[Float]("pitch", default = 0)
  val roll              = required[Float]("roll", default = 0)
  val trueHeading       = required[Float]("heading", default = 0)
  val magneticHeading   = required[Float]("magneticYaw", default = 0)
  val angleOfAttack     = required[Float]("AOA", default = 0)

  val velocity          = required[Vector3]("vectorVelocity", default = Vector3.Zero)
  val acceleration      = required[Vector3]("acc", default = Vector3.Zero)

  val indicatedAirspeed = required[Float]("IAS", default = 0)
  val trueAirspeed      = required[Float]("TAS", default = 0)
  val verticalVelocity  = required[Float]("vv", default = 0)
  val machNumber        = required[Float]("mach", default = 0)

  val altitudeAGL       = required[Float]("altitudeAboveGroundLevel", default = 0)
  val altitudeAsl       = required[Float]("altitudeAboveSeaLevel", default = 0)

  val windVelocity      = required[Vector3]("windVectorVelocity", default = Vector3.Zero)
  val airPressure       = required[Float]("atmospherePressure", default = 0)
  val slipBallDeviation = required[Float]("slipBallPosition", default = 0)

  val ilsLocalizer      = required[Float]("sideDeviation", default = 0)
  val ilsGlideslope     = required[Float]("glideDeviation", default = 0)
}

case class SensorAngles(source: SourceData = Map.empty) extends Parsed[SensorAngles.type] {
  val azimuth   = parse(schema.azimuth).toDegrees
  val elevation = parse(schema.elevation).toDegrees
}

object SensorAngles extends Schema[SensorAngles] {
  val azimuth   = required[Float]("azimuth", default = 0)
  val elevation = required[Float]("elevation", default = 0)
}

case class RadarDisplayScale(source: SourceData = Map.empty) extends Parsed[RadarDisplayScale.type] {
  val azimuth  = parse(schema.azimuth).toDegrees
  val distance = parse(schema.distance)
}

object RadarDisplayScale extends Schema[RadarDisplayScale] {
  val azimuth  = required[Float]("azimuth", default = 0)
  val distance = required[Float]("distance", default = 0)
}

case class MinMax(source: SourceData = Map.empty) extends Parsed[MinMax.type] {
  val min = parse(schema.min)
  val max = parse(schema.max)
}

object MinMax extends Schema[MinMax] {
  val min = required[Float]("min", default = 0)
  val max = required[Float]("max", default = 0)
}

case class TdcPosition(source: SourceData = Map.empty) extends Parsed[TdcPosition.type] {
  val x = parse(schema.x)
  val y = parse(schema.y)
}

object TdcPosition extends Schema[TdcPosition] {
  val x = required[Float]("x", default = 0)
  val y = required[Float]("y", default = 0)
}

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

case class ScanZone(source: SourceData = Map.empty) extends Parsed[ScanZone.type] {
  val altitudeCoverage = parse(schema.altitudeCoverage)
  val size             = parse(schema.size)
  val direction        = parse(schema.direction)
}

object ScanZone extends Schema[ScanZone] {
  val altitudeCoverage = required[MinMax]("coverage_H", default = MinMax())
  val size             = required[SensorAngles]("size", default = SensorAngles())
  val direction        = required[SensorAngles]("position", default = SensorAngles())
}

case class Prf(source: SourceData = Map.empty) extends Parsed[Prf.type] {
  val selection = parse(schema.selection)
  val current   = parse(schema.current)
}

object Prf extends Schema[Prf] {
  val selection = required[String]("selection", default = "")
  val current   = required[String]("current", default = "")
}

case class Dlz(source: SourceData = Map.empty) extends Parsed[Dlz.type] {
  val rAero = parse(schema.rAero)
  val rMin  = parse(schema.rMin)
  val rPi   = parse(schema.rPi)
  val rTr   = parse(schema.rTr)
}

object Dlz extends Schema[Dlz] {
  val rAero = required[Float]("RAERO", default = 0)
  val rMin  = required[Float]("RMIN", default = 0)
  val rPi   = required[Float]("RPI", default = 0)
  val rTr   = required[Float]("RTR", default = 0)
}

case class Target(source: SourceData) extends Parsed[Target.type] {
  val contact = parse(schema.contact)
  val dlz     = parse(schema.dlz)
}

object Target extends Schema[Target] {
  val contact = required[Contact]("target")
  val dlz     = required[Dlz]("DLZ")
  implicit def target2Contact(target: Target): Contact = target.contact
}

case class Contact(source: SourceData) extends Parsed[Contact.type] {
  val id                     = parse(schema.id)
  val course                 = parse(schema.course).toDegrees
  val flags                  = parse(schema.flags)
  val aspect                 = parse(schema.aspect).toDegrees
  val verticalViewingAngle   = parse(schema.verticalViewingAngle).toDegrees
  val horizontalViewingAngle = parse(schema.horizontalViewingAngle).toDegrees
  val updatesNumber          = parse(schema.updatesNumber)
  val startOfLock            = parse(schema.startOfLock)
  val rcs                    = parse(schema.rcs)
  val forces                 = parse(schema.forces)
  val country                = parse(schema.country)
  val burnthrough            = parse(schema.burnthrough)
  val jamming                = parse(schema.jamming) != 0
  val closure                = parse(schema.closure)
  val machNumber             = parse(schema.machNumber)
  val spatial                = parse(schema.spatial)
  val typ                    = parse(schema.typ)
  val velocity               = parse(schema.velocity)
  val distance               = parse(schema.distance)
}

object Contact extends Schema[Contact] {
  val id                     = required[Int]("ID")
  val course                 = required[Float]("course")
  val flags                  = required[Int]("flags")
  val aspect                 = required[Float]("delta_psi")
  val verticalViewingAngle   = required[Float]("fin")
  val horizontalViewingAngle = required[Float]("fim")
  val updatesNumber          = required[Int]("updates_number")
  val startOfLock            = required[Double]("start_of_lock")
  val rcs                    = required[Double]("reflection")
  val forces                 = required[Vector3]("forces")
  val country                = required[Int]("country")
  val burnthrough            = required[Boolean]("jammer_burned")
  val jamming                = required[Int]("isjamming")
  val closure                = required[Float]("convergence_velocity")
  val machNumber             = required[Float]("mach")
  val spatial                = required[Spatial]("position")
  val typ                    = required[UnitType]("type")
  val velocity               = required[Vector3]("velocity")
  val distance               = required[Float]("distance")
}

case class Targets(source: SourceData = Map.empty) extends Parsed[Targets.type] {
  val detected = parse(schema.detected)
  val tws      = parse(schema.tws)
  val locked   = parse(schema.locked)
}

object Targets extends Schema[Targets] {
  val detected = required[Seq[Contact]]("detected", default = Seq.empty)
  val tws      = required[Seq[Target]]("tws", default = Seq.empty)
  val locked   = required[Seq[Target]]("locked", default = Seq.empty)
}

case class Sensors(source: SourceData = Map.empty) extends Parsed[Sensors.type] {
  val status = parse(schema.status)
  val targets = parse(schema.targets)
}

object Sensors extends Schema[Sensors] {
  val status  = required[SensorsStatus]("status", default = SensorsStatus())
  val targets = required[Targets]("targets", default = Targets())
}

case class NavRequirements(source: SourceData = Map.empty) extends Parsed[NavRequirements.type] {
  val altitude      = parse(schema.altitude)
  val verticalSpeed = parse(schema.verticalSpeed)
  val roll          = parse(schema.roll).toDegrees
  val pitch         = parse(schema.pitch).toDegrees
  val speed         = parse(schema.speed)
}

object NavRequirements extends Schema[NavRequirements] {
  val altitude      = required[Float]("altitude", default = 0)
  val verticalSpeed = required[Float]("vertical_speed", default = 0)
  val roll          = required[Float]("roll", default = 0)
  val pitch         = required[Float]("pitch", default = 0)
  val speed         = required[Float]("speed", default = 0)
}

case class Acs(source: SourceData = Map.empty) extends Parsed[Acs.type] {
  val autoThrust = parse(schema.autoThrust)
  val mode       = parse(schema.mode)
}

object Acs extends Schema[Acs] {
  val autoThrust = required[Boolean]("autothrust", default = false)
  val mode       = required[String]("mode", default = "UKN")
}

case class AircraftMode(source: SourceData = Map.empty) extends Parsed[AircraftMode.type] {
  val submode = parse(schema.submode)
  val master  = parse(schema.master)
}

object AircraftMode extends Schema[AircraftMode] {
  val submode = required[String]("submode", default = "NAV")
  val master  = required[String]("master", default = "NAV")
}

case class NavIndicators(source: SourceData = Map.empty) extends Parsed[NavIndicators.type] {
  val requirements = parse(schema.requirements)
  val acs          = parse(schema.acs)
  val mode         = parse(schema.mode)
}

object NavIndicators extends Schema[NavIndicators] {
  val requirements = required[NavRequirements]("Requirements", default = NavRequirements())
  val acs          = required[Acs]("ACS", default = Acs())
  val mode         = required[AircraftMode]("SystemMode", default = AircraftMode())
}

case class BeaconIndicators(source: SourceData = Map.empty) extends Parsed[BeaconIndicators.type] {
  val glideSlope  = parse(schema.glideslope)
  val localizer   = parse(schema.localizer)
  val outerMarker = parse(schema.outerMarker)
  val innerMarker = parse(schema.innerMarker)
}

object BeaconIndicators extends Schema[BeaconIndicators] {
  val glideslope  = required[Boolean]("glideslope_deviation_beacon_lock", default = false)
  val localizer   = required[Boolean]("course_deviation_beacon_lock", default = false)
  val outerMarker = required[Boolean]("airfield_far", default = false)
  val innerMarker = required[Boolean]("airfield_near", default = false)
}

case class LeftRight(source: SourceData = Map.empty) extends Parsed[LeftRight.type] {
  val left  = parse(schema.left)
  val right = parse(schema.right)
}

object LeftRight extends Schema[LeftRight] {
  val left  = required[Float]("left", default = 0)
  val right = required[Float]("right", default = 0)
}

case class StatusAndValue(source: SourceData = Map.empty) extends Parsed[StatusAndValue.type] {
  val status = parse(schema.status)
  val value  = parse(schema.value)
}

object StatusAndValue extends Schema[StatusAndValue] {
  val status = required[Int]("status", default = 0)
  val value  = required[Double]("value", default = 0)
}

case class EngineIndicators(source: SourceData = Map.empty) extends Parsed[EngineIndicators.type] {
  val engineStart       = parse(schema.engineStart)
  val hydraulicPressure = parse(schema.hydraulicPressure)
  val fuelConsumption   = parse(schema.fuelConsumption)
  val rpm               = parse(schema.rpm)
  val temperature       = parse(schema.temperature)
  val fuelInternal      = parse(schema.fuelInternal)
  val fuelExternal      = parse(schema.fuelExternal)
}

object EngineIndicators extends Schema[EngineIndicators] {
  val engineStart       = required[LeftRight]("EngineStart", default = LeftRight())
  val hydraulicPressure = required[LeftRight]("HydraulicPressure", default = LeftRight())
  val fuelConsumption   = required[LeftRight]("FuelConsumption", default = LeftRight())
  val rpm               = required[LeftRight]("RPM", default = LeftRight())
  val temperature       = required[LeftRight]("Temperature", default = LeftRight())
  val fuelInternal      = required[Double]("fuel_internal", default = 0)
  val fuelExternal      = required[Double]("fuel_external", default = 0)
}

case class Wheel(source: SourceData = Map.empty) extends Parsed[Wheel.type] {
  val rod = parse(schema.rod)
}

object Wheel extends Schema[Wheel] {
  val rod = required[Float]("rod", default = 0)
}

case class BackWheels(source: SourceData = Map.empty) extends Parsed[BackWheels.type] {
  val left  = parse(schema.left)
  val right = parse(schema.right)
}

object BackWheels extends Schema[BackWheels] {
  val left  = required[Wheel]("left", default = Wheel())
  val right = required[Wheel]("right", default = Wheel())
}

case class Gear(source: SourceData = Map.empty) extends Parsed[Gear.type] {
  val value  = parse(schema.value)
  val nose   = parse(schema.nose)
  val main   = parse(schema.main)
  val status = parse(schema.status)
}

object Gear extends Schema[Gear] {
  val value  = required[Float]("value", default = 0)
  val nose   = required[Wheel]("nose", default = Wheel())
  val main   = required[BackWheels]("main", default = BackWheels())
  val status = required[Int]("status", default = 0)
}

case class ControlSurfaces(source: SourceData = Map.empty) extends Parsed[ControlSurfaces.type] {
  val aileron  = parse(schema.aileron)
  val elevator = parse(schema.elevator)
  val rudder   = parse(schema.rudder)
}

object ControlSurfaces extends Schema[ControlSurfaces] {
  val aileron  = required[LeftRight]("eleron", default = LeftRight())
  val elevator = required[LeftRight]("elevator", default = LeftRight())
  val rudder   = required[LeftRight]("rudder", default = LeftRight())
}

case class MechIndicators(source: SourceData = Map.empty) extends Parsed[MechIndicators.type] {
  val wheelbrakes     = parse(schema.wheelbrakes)
  val wing            = parse(schema.wing)
  val gear            = parse(schema.gear)
  val controlSurfaces = parse(schema.controlSurfaces)
  val airintake       = parse(schema.airintake)
  val refuelingboom   = parse(schema.refuelingboom)
  val flaps           = parse(schema.flaps)
  val parachute       = parse(schema.parachute)
  val noseflap        = parse(schema.noseflap)
  val hook            = parse(schema.hook)
  val speedbrakes     = parse(schema.speedbrakes)
  val canopy          = parse(schema.canopy)
}

object MechIndicators extends Schema[MechIndicators] {
  val wheelbrakes     = required[StatusAndValue]("wheelbrakes", default = StatusAndValue())
  val wing            = required[StatusAndValue]("wing", default = StatusAndValue())
  val gear            = required[Gear]("gear", default = Gear())
  val controlSurfaces = required[ControlSurfaces]("controlsurfaces", default = ControlSurfaces())
  val airintake       = required[StatusAndValue]("airintake", default = StatusAndValue())
  val refuelingboom   = required[StatusAndValue]("refuelingboom", default = StatusAndValue())
  val flaps           = required[StatusAndValue]("flaps", default = StatusAndValue())
  val parachute       = required[StatusAndValue]("parachute", default = StatusAndValue())
  val noseflap        = required[StatusAndValue]("noseflap", default = StatusAndValue())
  val hook            = required[StatusAndValue]("hook", default = StatusAndValue())
  val speedbrakes     = required[StatusAndValue]("speedbrakes", default = StatusAndValue())
  val canopy          = required[StatusAndValue]("canopy", default = StatusAndValue())
}

case class FailureIndicators(source: SourceData = Map.empty) extends Parsed[FailureIndicators.type] {
  val canopyOpen            = parse(schema.canopyOpen)
  val cannonFailure         = parse(schema.cannonFailure)
  val rightTailPlaneFailure = parse(schema.rightTailPlaneFailure)
  val leftAileronFailure    = parse(schema.leftAileronFailure)
  val autopilotFailure      = parse(schema.autopilotFailure)
  val hydraulicsFailure     = parse(schema.hydraulicsFailure)
  val hudFailure            = parse(schema.hudFailure)
  val rightWingPumpFailure  = parse(schema.rightWingPumpFailure)
  val leftWingPumpFailure   = parse(schema.leftWingPumpFailure)
  val ecmFailure            = parse(schema.ecmFailure)
  val rightEngineFailure    = parse(schema.rightEngineFailure)
  val stallSignalization    = parse(schema.stallSignalization)
  val helmetFailure         = parse(schema.helmetFailure)
  val radarFailure          = parse(schema.radarFailure)
  val rightMainPumpFailure  = parse(schema.rightMainPumpFailure)
  val acsFailure            = parse(schema.acsFailure)
  val mfdFailure            = parse(schema.mfdFailure)
  val leftEngineFailure     = parse(schema.leftEngineFailure)
  val leftTailPlaneFailure  = parse(schema.leftTailPlaneFailure)
  val mlwsFailure           = parse(schema.mlwsFailure)
  val eosFailure            = parse(schema.eosFailure)
  val autopilotOn           = parse(schema.autopilotOn)
  val leftMainPumpFailure   = parse(schema.leftMainPumpFailure)
  val rightAileronFailure   = parse(schema.rightAileronFailure)
  val rwsFailure            = parse(schema.rwsFailure)
  val masterCaution         = parse(schema.masterCaution)
  val fuelTankDamage        = parse(schema.fuelTankDamage)
  val hearFailure           = parse(schema.hearFailure)
}

object FailureIndicators extends Schema[FailureIndicators] {
  val canopyOpen            = required[Boolean]("CanopyOpen", default = false)
  val cannonFailure         = required[Boolean]("CannonFailure", default = false)
  val rightTailPlaneFailure = required[Boolean]("RightTailPlaneFailure", default = false)
  val leftAileronFailure    = required[Boolean]("LeftAileronFailure", default = false)
  val autopilotFailure      = required[Boolean]("AutopilotFailure", default = false)
  val hydraulicsFailure     = required[Boolean]("HydraulicsFailure", default = false)
  val hudFailure            = required[Boolean]("HUDFailure", default = false)
  val rightWingPumpFailure  = required[Boolean]("RightWingPumpFailure", default = false)
  val leftWingPumpFailure   = required[Boolean]("LeftWingPumpFailure", default = false)
  val ecmFailure            = required[Boolean]("ECMFailure", default = false)
  val rightEngineFailure    = required[Boolean]("RightEngineFailure", default = false)
  val stallSignalization    = required[Boolean]("StallSignalization", default = false)
  val helmetFailure         = required[Boolean]("HelmetFailure", default = false)
  val radarFailure          = required[Boolean]("RadarFailure", default = false)
  val rightMainPumpFailure  = required[Boolean]("RightMainPumpFailure", default = false)
  val acsFailure            = required[Boolean]("ACSFailure", default = false)
  val mfdFailure            = required[Boolean]("MFDFailure", default = false)
  val leftEngineFailure     = required[Boolean]("LeftEngineFailure", default = false)
  val leftTailPlaneFailure  = required[Boolean]("LeftTailPlaneFailure", default = false)
  val mlwsFailure           = required[Boolean]("MLWSFailure", default = false)
  val eosFailure            = required[Boolean]("EOSFailure", default = false)
  val autopilotOn           = required[Boolean]("AutopilotOn", default = false)
  val leftMainPumpFailure   = required[Boolean]("LeftMainPumpFailure", default = false)
  val rightAileronFailure   = required[Boolean]("RightAileronFailure", default = false)
  val rwsFailure            = required[Boolean]("RWSFailure", default = false)
  val masterCaution         = required[Boolean]("MasterWarning", default = false)
  val fuelTankDamage        = required[Boolean]("FuelTankDamage", default = false)
  val hearFailure           = required[Boolean]("GearFailure", default = false)
}

case class HsiIndicators(source: SourceData = Map.empty) extends Parsed[HsiIndicators.type] {
  val rmiRaw          = parse(schema.rmiRaw).toDegrees
  val courseDeviation = parse(schema.courseDeviation).toDegrees
  val course          = parse(schema.course).toDegrees
  val headingRaw      = parse(schema.headingRaw).toDegrees
  val headingPointer  = parse(schema.headingPointer).toDegrees
  val bearingPointer  = parse(schema.bearingPointer).toDegrees
  val adfRaw          = parse(schema.adfRaw).toDegrees
}

object HsiIndicators extends Schema[HsiIndicators] {
  val rmiRaw          = required[Float]("RMI_raw", default = 0)
  val courseDeviation = required[Float]("CourseDeviation", default = 0)
  val course          = required[Float]("Course", default = 0)
  val headingRaw      = required[Float]("Heading_raw", default = 0)
  val headingPointer  = required[Float]("HeadingPointer", default = 0)
  val bearingPointer  = required[Float]("BearingPointer", default = 0)
  val adfRaw          = required[Float]("ADF_raw", default = 0)
}

case class Indicators(source: SourceData = Map.empty) extends Parsed[Indicators.type] {
  val nav               = parse(schema.nav)
  val beacons           = parse(schema.beacons)
  val engines           = parse(schema.engines)
  val mechIndicators    = parse(schema.mechIndicators)
  val failureIndicators = parse(schema.failureIndicators)
  val hsiIndicators     = parse(schema.hsiIndicators)
}

object Indicators extends Schema[Indicators] {
  val nav               = required[NavIndicators]("nav", default = NavIndicators())
  val beacons           = required[BeaconIndicators]("beacon", default = BeaconIndicators())
  val engines           = required[EngineIndicators]("engines", default = EngineIndicators())
  val mechIndicators    = required[MechIndicators]("mech", default = MechIndicators())
  val failureIndicators = required[FailureIndicators]("failures", default = FailureIndicators())
  val hsiIndicators     = required[HsiIndicators]("HSI", default = HsiIndicators())
}

case class Spatial(source: SourceData = Map.empty) extends Parsed[Spatial.type] {
  val x        = parse(schema.x)
  val y        = parse(schema.y)
  val z        = parse(schema.z)
  val position = parse(schema.position)
}

object Spatial extends Schema[Spatial] {
  val x        = required[Vector3]("x", default = Vector3.Zero)
  val y        = required[Vector3]("y", default = Vector3.Zero)
  val z        = required[Vector3]("z", default = Vector3.Zero)
  val position = required[Vector3]("p", default = Vector3.Zero)
}

case class AiWingman(source: SourceData) extends Parsed[AiWingman.type] {
  val orderedTask     = parse(schema.orderedTask)
  val currentTarget   = parse(schema.currentTarget)
  val currentTask     = parse(schema.currentTask)
  val wingmenPosition = parse(schema.wingmenPosition)
  val wingmenId       = parse(schema.wingmenId)
  val orderedTarget   = parse(schema.orderedTarget)
}

object AiWingman extends Schema[AiWingman] {
  val orderedTask     = required[String]("ordered_task")
  val currentTarget   = required[Int]("current_target")
  val currentTask     = required[String]("current_task")
  val wingmenPosition = required[Spatial]("wingmen_position")
  val wingmenId       = required[Int]("wingmen_id")
  val orderedTarget   = required[Int]("ordered_target")
}

case class Waypoint(source: SourceData = Map.empty) extends Parsed[Waypoint.type] {
  val position      = parse(schema.position)
  val estimatedTime = parse(schema.estimatedTime)
  val speedReq      = parse(schema.speedReq)
  val nextPointNum  = parse(schema.nextPointNum)
  val pointAction   = parse(schema.pointAction)
  val thisPointNum  = parse(schema.thisPointNum)
}

object Waypoint extends Schema[Waypoint] {
  val position      = required[Vector3]("world_point", default = Vector3.Zero)
  val estimatedTime = required[Double]("estimated_time", default = 0)
  val speedReq      = required[Double]("speed_req", default = 0)
  val nextPointNum  = required[Int]("next_point_num", default = 0)
  val pointAction   = required[String]("point_action", default = "")
  val thisPointNum  = required[Int]("this_point_num", default = 0)
}

case class Route(source: SourceData = Map.empty) extends Parsed[Route.type] {
  val waypoints       = parse(schema.waypoints)
  val currentWaypoint = parse(schema.currentWaypoint)
}

object Route extends Schema[Route] {
  val waypoints       = required[Seq[Waypoint]]("route", default = Seq.empty)
  val currentWaypoint = required[Waypoint]("goto_point", default = Waypoint())
}

case class Version(source: SourceData = Map.empty) extends Parsed[Version.type] {
  val productName    = parse(schema.productName)
  val fileVersion    = parse(schema.fileVersion).mkString(".")
  val productVersion = parse(schema.productVersion).mkString(".")
}

object Version extends Schema[Version] {
  val productName    = required[String]("ProductName", default = "")
  val fileVersion    = required[Seq[Int]]("FileVersion", default = Seq.empty)
  val productVersion = required[Seq[Int]]("ProductVersion", default = Seq.empty)
}

case class StatusFlags(source: SourceData = Map.empty) extends Parsed[StatusFlags.type] {
  val aiOn        = parse(schema.aiOn)
  val jamming     = parse(schema.jamming)
  val born        = parse(schema.born)
  val static      = parse(schema.static)
  val human       = parse(schema.human)
  val radarActive = parse(schema.radarActive)
  val iRJamming   = parse(schema.iRJamming)
  val invisible   = parse(schema.invisible)
}

object StatusFlags extends Schema[StatusFlags] {
  val aiOn        = required[Boolean]("AI_ON", default = false)
  val jamming     = required[Boolean]("Jamming", default = false)
  val born        = required[Boolean]("Born", default = false)
  val static      = required[Boolean]("Static", default = false)
  val human       = required[Boolean]("Human", default = false)
  val radarActive = required[Boolean]("RadarActive", default = false)
  val iRJamming   = required[Boolean]("IRJamming", default = false)
  val invisible   = required[Boolean]("Invisible", default = false)
}

case class GeoPosition(source: SourceData = Map.empty) extends Parsed[GeoPosition.type] {
  val lat = parse(schema.lat).toDegrees
  val lon = parse(schema.lon).toDegrees
  val alt = parse(schema.alt).toDegrees
}

object GeoPosition extends Schema[GeoPosition] {
  val lat = required[Double]("Lat", default = 0)
  val lon = required[Double]("Long", default = 0)
  val alt = required[Double]("Alt", default = 0)
}

case class SelfData(source: SourceData = Map.empty) extends Parsed[SelfData.type] {
  val statusFlags  = parse(schema.statusFlags)
  val callsign     = parse(schema.callsign)
  val aircraftName = parse(schema.aircraftName)
  val geoPosition  = parse(schema.geoPosition)
  val coalition    = parse(schema.coalition)
  val country      = parse(schema.country)
  val position     = parse(schema.position)
  val groupname    = parse(schema.groupname)
  val coalitionId  = parse(schema.coalitionId)
  val pitch        = parse(schema.pitch).toDegrees
  val roll         = parse(schema.roll).toDegrees
  val heading      = parse(schema.heading).toDegrees
  val typ          = parse(schema.typ)
}

object SelfData extends Schema[SelfData] {
  val statusFlags  = required[StatusFlags]("Flags", default = StatusFlags())
  val callsign     = required[String]("UnitName", default = "")
  val aircraftName = required[String]("Name", default = "")
  val geoPosition  = required[GeoPosition]("LatLongAlt", default = GeoPosition())
  val coalition    = required[String]("Coalition", default = "")
  val country      = required[Int]("Country", default = 0)
  val position     = required[Vector3]("Position", default = Vector3.Zero)
  val groupname    = required[String]("GroupName", default = "")
  val coalitionId  = required[Int]("CoalitionID", default = 0)
  val pitch        = required[Float]("Pitch", default = 0)
  val roll         = required[Float]("Bank", default = 0)
  val heading      = required[Float]("Heading", default = 0)
  val typ          = required[UnitType]("Type", default = UnitType())
}

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