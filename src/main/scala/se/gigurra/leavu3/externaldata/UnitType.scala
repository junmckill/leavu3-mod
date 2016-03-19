package se.gigurra.leavu3.externaldata

import com.twitter.util.{Duration, JavaTimer}
import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.Schema
import se.gigurra.leavu3.Configuration
import se.gigurra.leavu3.util.{Eventually, RestClient}
import se.gigurra.serviceutils.json.JSON
import se.gigurra.serviceutils.twitter.logging.Logging

import scala.collection.concurrent
import scala.language.implicitConversions

case class UnitTypeData(fullName: String,
                        shortName: String,
                        isKnown: Boolean,
                        typ: UnitType.TYPE,
                        power2RangeExponent: Double,
                        maxRangeIndication: Double)

object UnitTypeData {
  val DEFAULT_POWER_MAPPING_EXPONENT = 2.0
  val AWACS_POWER_MAPPING_EXPONENT = 7.0
  val DEFAULT_MAX_RANGE_INDICATION = 1.0
  val DEFAULT_ARH_MAX_RANGE_INDICATION = 0.4
  val UKN = UnitTypeData("unknown", "UKN", isKnown = false, (0,0,0,0), DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)

  def defaultShortName(fullName: String) =Data(fullName.split(' ').head.toUpperCase, DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)

  case class Data(shortName: String, power2RangeExponent: Double, maxRangeIndication: Double)

  def firstNumber(text: String): String = {
    val iFirst = text.indexWhere(_.isDigit)
    if (iFirst >= 0) {
      val iLast = text.drop(iFirst).indexWhere(!_.isDigit)
      if (iLast > 0) {
        text.substring(iFirst, iLast + iFirst)
      } else {
        text.substring(iFirst, iFirst + 1)
      }
    } else {
      "ukn"
    }
  }

  val unitMappings: PartialFunction[String, Data] = {

    // Fighters
    case x if x.startsWith("mig-29")    => Data("29",           DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("su-27")     => Data("29",           DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("su-3")      => Data("30",           DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("su-")       => Data(firstNumber(x), DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("mig-")      => Data(firstNumber(x), DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("f-")        => Data(firstNumber(x), DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)

    case x if x.startsWith("e-3")       => Data("E3",           AWACS_POWER_MAPPING_EXPONENT,   DEFAULT_MAX_RANGE_INDICATION)

    // SAMs
    case x if x.startsWith("s-300")     => Data("10",           DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("csa")       => Data("8",            DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("tunguska")  => Data("19",           DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("tor")       => Data("15",           DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)

    // Active missiles
    case x if x.startsWith("aim-120")   => Data("M120",         DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_ARH_MAX_RANGE_INDICATION)
    case x if x.startsWith("aim-154")   => Data("M54",          DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_ARH_MAX_RANGE_INDICATION)
    case x if x.startsWith("r-77")      => Data("M77",          DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_ARH_MAX_RANGE_INDICATION)
  }

  def apply(fullName: String, isKnown: Boolean, typ: UnitType.TYPE): UnitTypeData = {
    val data = unitMappings.applyOrElse(fullName.toLowerCase, defaultShortName)
    UnitTypeData(fullName, data.shortName, isKnown, typ, data.power2RangeExponent, data.maxRangeIndication)
  }
}

case class UnitType(source: SourceData = Map.empty) extends SafeParsed[UnitType.type] {
  val level1 = parse(schema.level1)
  val level2 = parse(schema.level2)
  val level3 = parse(schema.level3)
  val level4 = parse(schema.level4)

  def typ: UnitType.TYPE = {
    (level1, level2, level3, level4)
  }

  def isFlyer: Boolean = level1 == UnitType.AIR ||level1 == UnitType.WEAPON
}

object UnitType extends Schema[UnitType] with Logging {
  val level1 = required[Int]("level1", default = 0)
  val level2 = required[Int]("level2", default = 0)
  val level3 = required[Int]("level3", default = 0)
  val level4 = required[Int]("level4", default = 0)

  val AIR = 1
  val GROUND = 2
  val NAVY = 3
  val WEAPON = 4
  val STATIC = 5
  val DESTROYED = 6

  type TYPE = (Int, Int, Int, Int)
  val pendingTypes = new concurrent.TrieMap[TYPE, Boolean]()
  val mappedTypes = new concurrent.TrieMap[TYPE, UnitTypeData]()
  var clientSingleton = Eventually[RestClient]()

  val timer = new JavaTimer(isDaemon = true)

  def getData(t: UnitType)(implicit c: Configuration): UnitTypeData = {

    if (!mappedTypes.contains(t.typ) && !pendingTypes.contains(t.typ)) {

      pendingTypes.put(t.typ, true)

      val client = clientSingleton.get(RestClient(c.dcsRemoteAddress, c.dcsRemotePort))
      val script =s"{name=LoGetNameByType${t.typ}}"
      val url = s"export/$script"

      client.get(url, cacheMaxAgeMillis = Some(3600000L), timeout = Duration.fromSeconds(1)).onSuccess { json =>
        val name = JSON.readMap(json)("name").asInstanceOf[String]
        mappedTypes.put(t.typ, UnitTypeData.apply(name, isKnown = true, t.typ))
        pendingTypes.remove(t.typ)
        logger.info(s"Mapped type ${t.typ} -> $name")
      }.onFailure { e =>
        pendingTypes.remove(t.typ)
      }
    }

    mappedTypes.getOrElse(t.typ, UnitTypeData.UKN)
  }

  implicit def UnitType2Data(t: UnitType)(implicit c: Configuration): UnitTypeData = getData(t)
}
