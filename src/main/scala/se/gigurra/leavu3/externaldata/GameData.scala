package se.gigurra.leavu3.externaldata

import scala.language.implicitConversions
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
  val level1 = parse(schema.level1)
  val level2 = parse(schema.level2)
  val level3 = parse(schema.level3)
  val level4 = parse(schema.level4)
}

object UnitType extends Schema[UnitType] {
  val level1 = required[Int]("level1")
  val level2 = required[Int]("level2")
  val level3 = required[Int]("level3")
  val level4 = required[Int]("level4")
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

case class Dlz(source: SourceData) extends Parsed[Dlz.type] {
  val rAero = parse(schema.rAero)
  val rMin  = parse(schema.rMin)
  val rPi   = parse(schema.rPi)
  val rTr   = parse(schema.rTr)
}

object Dlz extends Schema[Dlz] {
  val rAero = required[Double]("RAERO")
  val rMin  = required[Double]("RMIN")
  val rPi   = required[Double]("RPI")
  val rTr   = required[Double]("RTR")
}

case class Target(source: SourceData) extends Parsed[Target.type] {
  val contact = parse(schema.contact)
  val dlz     = parse(schema.dlz)
}

object Target extends Schema[Target] {
  val contact = required[Contact]("target")
  val dlz     = required[Dlz]("DLZ")
  implicit def tgt2ctct(target: Target): Contact = target.contact
}

case class Contact(source: SourceData) extends Parsed[Contact.type] {
  val id                     = parse(schema.id)
  val course                 = parse(schema.course)
  val flags                  = parse(schema.flags)
  val aspect                 = parse(schema.aspect)
  val verticalViewingAngle   = parse(schema.verticalViewingAngle)
  val horizontalViewingAngle = parse(schema.horizontalViewingAngle)
  val updatesNumber          = parse(schema.updatesNumber)
  val startOfLong            = parse(schema.startOfLong)
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
  val course                 = required[Double]("course")
  val flags                  = required[Int]("flags")
  val aspect                 = required[Double]("delta_psi")
  val verticalViewingAngle   = required[Double]("fin")
  val horizontalViewingAngle = required[Double]("fim")
  val updatesNumber          = required[Int]("updates_number")
  val startOfLong            = required[Double]("start_of_lock")
  val rcs                    = required[Double]("reflection")
  val forces                 = required[Vector3]("forces")
  val country                = required[Int]("country")
  val burnthrough            = required[Boolean]("jammer_burned")
  val jamming                = required[Int]("isjamming")
  val closure                = required[Double]("convergence_velocity")
  val machNumber             = required[Double]("mach")
  val spatial                = required[Spatial]("position")
  val typ                    = required[UnitType]("type")
  val velocity               = required[Vector3]("velocity")
  val distance               = required[Double]("distance")
}


case class Targets(source: SourceData) extends Parsed[Targets.type] {
  val detected = parse(schema.detected)
  val tws      = parse(schema.tws)
  val locked   = parse(schema.locked)
}

object Targets extends Schema[Targets] {
  val detected = required[Seq[Contact]]("detected")
  val tws      = required[Seq[Target]]("tws")
  val locked   = required[Seq[Target]]("locked")
}

case class Sensors(source: SourceData) extends Parsed[Sensors.type] {
  val status = parse(schema.status)
  val targets = parse(schema.targets)
}

object Sensors extends Schema[Sensors] {
  val status  = required[SensorsStatus]("status")
  val targets = required[Targets]("targets")
}

case class NavRequirements(source: SourceData) extends Parsed[NavRequirements.type] {
  val altitude      = parse(schema.altitude)
  val verticalSpeed = parse(schema.verticalSpeed)
  val roll          = parse(schema.roll)
  val pitch         = parse(schema.pitch)
  val speed         = parse(schema.speed)
}

object NavRequirements extends Schema[NavRequirements] {
  val altitude      = required[Double]("altitude")
  val verticalSpeed = required[Double]("vertical_speed")
  val roll          = required[Double]("roll")
  val pitch         = required[Double]("pitch")
  val speed         = required[Double]("speed")
}

case class Acs(source: SourceData) extends Parsed[Acs.type] {
  val autoThrust = parse(schema.autoThrust)
  val mode       = parse(schema.mode)
}

object Acs extends Schema[Acs] {
  val autoThrust = required[Boolean]("autothrust")
  val mode       = required[String]("mode")
}

case class AircraftMode(source: SourceData) extends Parsed[AircraftMode.type] {
  val submode = parse(schema.submode)
  val master  = parse(schema.master)
}

object AircraftMode extends Schema[AircraftMode] {
  val submode = required[String]("submode")
  val master  = required[String]("master")
}

case class NavIndicators(source: SourceData) extends Parsed[NavIndicators.type] {
  val requirements = parse(schema.requirements)
  val acs          = parse(schema.acs)
  val mode         = parse(schema.mode)
}

object NavIndicators extends Schema[NavIndicators] {
  val requirements = required[NavRequirements]("Requirements")
  val acs          = required[Acs]("ACS")
  val mode         = required[AircraftMode]("SystemMode")
}

case class BeaconIndicators(source: SourceData) extends Parsed[BeaconIndicators.type] {
  val glideSlope  = parse(schema.glideslope)
  val localizer   = parse(schema.localizer)
  val outerMarker = parse(schema.outerMarker)
  val innerMarker = parse(schema.innerMarker)
}

object BeaconIndicators extends Schema[BeaconIndicators] {
  val glideslope  = required[Boolean]("glideslope_deviation_beacon_lock")
  val localizer   = required[Boolean]("course_deviation_beacon_lock")
  val outerMarker = required[Boolean]("airfield_far")
  val innerMarker = required[Boolean]("airfield_near")
}

case class LeftRight(source: SourceData) extends Parsed[LeftRight.type] {
  val left  = parse(schema.left)
  val right = parse(schema.right)
}

object LeftRight extends Schema[LeftRight] {
  val left  = required[Double]("left")
  val right = required[Double]("right")
}

case class StatusAndValue(source: SourceData) extends Parsed[StatusAndValue.type] {
  val status = parse(schema.status)
  val value  = parse(schema.value)
}

object StatusAndValue extends Schema[StatusAndValue] {
  val status = required[Int]("status")
  val value  = required[Double]("value")
}

case class EngineIndicators(source: SourceData) extends Parsed[EngineIndicators.type] {
  val engineStart       = parse(schema.engineStart)
  val hydraulicPressure = parse(schema.hydraulicPressure)
  val fuelConsumption   = parse(schema.fuelConsumption)
  val rpm               = parse(schema.rpm)
  val temperature       = parse(schema.temperature)
  val fuelInternal      = parse(schema.fuelInternal)
  val fuelExternal      = parse(schema.fuelExternal)
}

object EngineIndicators extends Schema[EngineIndicators] {
  val engineStart       = required[LeftRight]("EngineStart")
  val hydraulicPressure = required[LeftRight]("HydraulicPressure")
  val fuelConsumption   = required[LeftRight]("FuelConsumption")
  val rpm               = required[LeftRight]("RPM")
  val temperature       = required[LeftRight]("Temperature")
  val fuelInternal      = required[Double]("fuel_internal")
  val fuelExternal      = required[Double]("fuel_external")
}

case class Wheel(source: SourceData) extends Parsed[Wheel.type] {
  val rod = parse(schema.rod)
}

object Wheel extends Schema[Wheel] {
  val rod = required[Double]("rod")
}

case class BackWheels(source: SourceData) extends Parsed[BackWheels.type] {
  val left  = parse(schema.left)
  val right = parse(schema.right)
}

object BackWheels extends Schema[BackWheels] {
  val left  = required[Wheel]("left")
  val right = required[Wheel]("right")
}

case class Gear(source: SourceData) extends Parsed[Gear.type] {
  val value  = parse(schema.value)
  val nose   = parse(schema.nose)
  val main   = parse(schema.main)
  val status = parse(schema.status)
}

object Gear extends Schema[Gear] {
  val value  = required[Double]("value")
  val nose   = required[Wheel]("nose")
  val main   = required[BackWheels]("main")
  val status = required[Int]("status")
}

case class ControlSurfaces(source: SourceData) extends Parsed[ControlSurfaces.type] {
  val aileron  = parse(schema.aileron)
  val elevator = parse(schema.elevator)
  val rudder   = parse(schema.rudder)
}

object ControlSurfaces extends Schema[ControlSurfaces] {
  val aileron  = required[LeftRight]("eleron")
  val elevator = required[LeftRight]("elevator")
  val rudder   = required[LeftRight]("rudder")
}

case class MechIndicators(source: SourceData) extends Parsed[MechIndicators.type] {
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
  val wheelbrakes     = required[StatusAndValue]("wheelbrakes")
  val wing            = required[StatusAndValue]("wing")
  val gear            = required[Gear]("gear")
  val controlSurfaces = required[ControlSurfaces]("controlsurfaces")
  val airintake       = required[StatusAndValue]("airintake")
  val refuelingboom   = required[StatusAndValue]("refuelingboom")
  val flaps           = required[StatusAndValue]("flaps")
  val parachute       = required[StatusAndValue]("parachute")
  val noseflap        = required[StatusAndValue]("noseflap")
  val hook            = required[StatusAndValue]("hook")
  val speedbrakes     = required[StatusAndValue]("speedbrakes")
  val canopy          = required[StatusAndValue]("canopy")
}

case class FailureIndicators(source: SourceData) extends Parsed[FailureIndicators.type] {
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
  val canopyOpen            = required[Boolean]("CanopyOpen")
  val cannonFailure         = required[Boolean]("CannonFailure")
  val rightTailPlaneFailure = required[Boolean]("RightTailPlaneFailure")
  val leftAileronFailure    = required[Boolean]("LeftAileronFailure")
  val autopilotFailure      = required[Boolean]("AutopilotFailure")
  val hydraulicsFailure     = required[Boolean]("HydraulicsFailure")
  val hudFailure            = required[Boolean]("HUDFailure")
  val rightWingPumpFailure  = required[Boolean]("RightWingPumpFailure")
  val leftWingPumpFailure   = required[Boolean]("LeftWingPumpFailure")
  val ecmFailure            = required[Boolean]("ECMFailure")
  val rightEngineFailure    = required[Boolean]("RightEngineFailure")
  val stallSignalization    = required[Boolean]("StallSignalization")
  val helmetFailure         = required[Boolean]("HelmetFailure")
  val radarFailure          = required[Boolean]("RadarFailure")
  val rightMainPumpFailure  = required[Boolean]("RightMainPumpFailure")
  val acsFailure            = required[Boolean]("ACSFailure")
  val mfdFailure            = required[Boolean]("MFDFailure")
  val leftEngineFailure     = required[Boolean]("LeftEngineFailure")
  val leftTailPlaneFailure  = required[Boolean]("LeftTailPlaneFailure")
  val mlwsFailure           = required[Boolean]("MLWSFailure")
  val eosFailure            = required[Boolean]("EOSFailure")
  val autopilotOn           = required[Boolean]("AutopilotOn")
  val leftMainPumpFailure   = required[Boolean]("LeftMainPumpFailure")
  val rightAileronFailure   = required[Boolean]("RightAileronFailure")
  val rwsFailure            = required[Boolean]("RWSFailure")
  val masterCaution         = required[Boolean]("MasterWarning")
  val fuelTankDamage        = required[Boolean]("FuelTankDamage")
  val hearFailure           = required[Boolean]("GearFailure")
}

case class HsiIndicators(source: SourceData) extends Parsed[HsiIndicators.type] {
  val rmiRaw          = parse(schema.rmiRaw)
  val courseDeviation = parse(schema.courseDeviation)
  val course          = parse(schema.course)
  val headingRaw      = parse(schema.headingRaw)
  val headingPointer  = parse(schema.headingPointer)
  val bearingPointer  = parse(schema.bearingPointer)
  val adfRaw          = parse(schema.adfRaw)
}

object HsiIndicators extends Schema[HsiIndicators] {
  val rmiRaw          = required[Double]("RMI_raw")
  val courseDeviation = required[Double]("CourseDeviation")
  val course          = required[Double]("Course")
  val headingRaw      = required[Double]("Heading_raw")
  val headingPointer  = required[Double]("HeadingPointer")
  val bearingPointer  = required[Double]("BearingPointer")
  val adfRaw          = required[Double]("ADF_raw")
}

case class Indicators(source: SourceData) extends Parsed[Indicators.type] {
  val nav               = parse(schema.nav)
  val beacons           = parse(schema.beacons)
  val engines           = parse(schema.engines)
  val mechIndicators    = parse(schema.mechIndicators)
  val failureIndicators = parse(schema.failureIndicators)
  val HsiIndicators     = parse(schema.HsiIndicators)
}

object Indicators extends Schema[Indicators] {
  val nav               = required[NavIndicators]("nav")
  val beacons           = required[BeaconIndicators]("beacon")
  val engines           = required[EngineIndicators]("engines")
  val mechIndicators    = required[MechIndicators]("mech")
  val failureIndicators = required[FailureIndicators]("failures")
  val HsiIndicators     = required[HsiIndicators]("HSI")
}

case class Spatial(source: SourceData) extends Parsed[Spatial.type] {
  val x        = parse(schema.x)
  val y        = parse(schema.y)
  val z        = parse(schema.z)
  val position = parse(schema.position)
}

object Spatial extends Schema[Spatial] {
  val x        = required[Vector3]("x")
  val y        = required[Vector3]("y")
  val z        = required[Vector3]("z")
  val position = required[Vector3]("p")
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

case class Waypoint(source: SourceData) extends Parsed[Waypoint.type] {
  val position      = parse(schema.position)
  val estimatedTime = parse(schema.estimatedTime)
  val speedReq      = parse(schema.speedReq)
  val nextPointNum  = parse(schema.nextPointNum)
  val pointAction   = parse(schema.pointAction)
  val thisPointNum  = parse(schema.thisPointNum)
}

object Waypoint extends Schema[Waypoint] {
  val position      = required[Vector3]("world_point")
  val estimatedTime = required[Double]("estimated_time")
  val speedReq      = required[Double]("speed_req")
  val nextPointNum  = required[Double]("next_point_num")
  val pointAction   = required[Double]("point_action")
  val thisPointNum  = required[Double]("this_point_num")
}

case class Route(source: SourceData) extends Parsed[Route.type] {
  val waypoints       = parse(schema.waypoints)
  val currentWaypoint = parse(schema.currentWaypoint)
}

object Route extends Schema[Route] {
  val waypoints       = required[Seq[Waypoint]]("route")
  val currentWaypoint = required[Waypoint]("goto_point")
}

case class Version(source: SourceData) extends Parsed[Version.type] {
  val productName    = parse(schema.productName)
  val fileVersion    = parse(schema.fileVersion).mkString(".")
  val productVersion = parse(schema.productVersion).mkString(".")
}

object Version extends Schema[Version] {
  val productName    = required[String]("ProductName")
  val fileVersion    = required[Seq[Int]]("FileVersion")
  val productVersion = required[Seq[Int]]("ProductVersion")
}

case class StatusFlags(source: SourceData) extends Parsed[StatusFlags.type] {
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
  val aiOn        = required[Boolean]("AI_ON")
  val jamming     = required[Boolean]("Jamming")
  val born        = required[Boolean]("Born")
  val static      = required[Boolean]("Static")
  val human       = required[Boolean]("Human")
  val radarActive = required[Boolean]("RadarActive")
  val iRJamming   = required[Boolean]("IRJamming")
  val invisible   = required[Boolean]("Invisible")
}

case class GeoPosition(source: SourceData) extends Parsed[GeoPosition.type] {
  val lat = parse(schema.lat)
  val lon = parse(schema.lon)
  val alt = parse(schema.alt)
}

object GeoPosition extends Schema[GeoPosition] {
  val lat = required[Double]("Lat")
  val lon = required[Double]("Long")
  val alt = required[Double]("Alt")
}

case class SelfData(source: SourceData) extends Parsed[SelfData.type] {
  val statusFlags  = parse(schema.statusFlags)
  val callsign     = parse(schema.callsign)
  val aircraftName = parse(schema.aircraftName)
  val geoPosition  = parse(schema.geoPosition)
  val coalition    = parse(schema.coalition)
  val country      = parse(schema.country)
  val position     = parse(schema.position)
  val groupname    = parse(schema.groupname)
  val coalitionId  = parse(schema.coalitionId)
  val pitch        = parse(schema.pitch)
  val roll         = parse(schema.roll)
  val heading      = parse(schema.heading)
  val typ          = parse(schema.typ)
}

object SelfData extends Schema[SelfData] {
  val statusFlags  = required[StatusFlags]("Flags")
  val callsign     = required[String]("UnitName")
  val aircraftName = required[String]("Name")
  val geoPosition  = required[GeoPosition]("LatLongAlt")
  val coalition    = required[String]("Coalition")
  val country      = required[Int]("Country")
  val position     = required[Vector3]("Position")
  val groupname    = required[String]("GroupName")
  val coalitionId  = required[Int]("CoalitionID")
  val pitch        = required[Double]("Pitch")
  val roll         = required[Double]("Bank")
  val heading      = required[Double]("Heading")
  val typ          = required[UnitType]("Type")
}

case class MetaData(source: SourceData) extends Parsed[MetaData.type] {
  val camera   = parse(schema.camera)
  val planeId  = parse(schema.planeId)
  val time     = parse(schema.time)
  val version  = parse(schema.version)
  val selfData = parse(schema.selfData)
}

object MetaData extends Schema[MetaData] {
  val camera   = required[Spatial]("camPos")
  val planeId  = required[Int]("playerPlaneId")
  val time     = required[Double]("modelTime")
  val version  = required[Version]("version")
  val selfData = required[SelfData]("selfData")
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
  val indicators      = parse(schema.indicators)
  val metaData        = parse(schema.metadata)
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
  val indicators      = optional[Indicators]("indicators")
  val aiWingmen       = optional[Seq[Option[AiWingman]]]("wingMen")
  val route           = optional[Route]("route")
  val metadata        = optional[MetaData]("metaData")


  ///////////////////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////


  val path = "export/dcs_remote_export_data()"

  def startPoller(fps: Int, addr: String, port: Int): Unit = {

    val client = RestClient(addr, port)

    SimpleTimer.fromFps(fps) {
      val stringData = client.getBlocking(path, cacheMaxAgeMillis = Some((1000.0 / fps / 2.0).toLong))
      ExternalData.gameData = JSON.read(stringData)
    }
  }

}


