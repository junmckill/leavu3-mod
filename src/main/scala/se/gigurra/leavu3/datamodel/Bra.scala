package se.gigurra.leavu3.datamodel

import se.gigurra.leavu3.lmath.{NormalizeDegrees, UnitConversions}

/**
  * Created by johan_home on 2016-03-25.
  */

case class Bra(bearingRaw: Double, range: Double, deltaAltitude: Double) extends UnitConversions {

  def toOffset: Vec3 = {
    val scale = math.cos(deltaAltitude / range)
    val y = range * math.cos(bearing.toRadians) * scale
    val x = range * math.sin(bearing.toRadians) * scale
    Vec3(x, y, deltaAltitude)
  }

  def bearing: Double = NormalizeDegrees._0360(bearingRaw)

  def bearingString: String = {

    def padBearing(in: String): String = {
      in.length match {
        case 0 => "000"
        case 1 => "00" + in
        case 2 => "0" + in
        case _ =>  in
      }
    }

    padBearing(bearing.round.toString)
  }

  def distString: String = {
    (range * m_to_nmi).round.toString
  }

  def brString: String = {
    s"$bearingString $distString"
  }

}
