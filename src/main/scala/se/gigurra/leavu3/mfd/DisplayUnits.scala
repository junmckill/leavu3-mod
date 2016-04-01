package se.gigurra.leavu3.mfd

import se.gigurra.leavu3.gfx.RenderContext._
import se.gigurra.leavu3.util.CircleBuffer

import scala.language.postfixOps

/**
  * Created by kjolh on 3/29/2016.
  */

case class DisplayUnits(name: String,
                        m_to_altUnit: Double,
                        m_to_distUnit: Double,
                        mps_to_speedUnit: Double,
                        distScale: CircleBuffer[Double])

object DisplayUnits {

  val imperialDistances = CircleBuffer(10 nmi, 20 nmi, 40 nmi, 80 nmi, 160 nmi).withDefaultValue(40 nmi)
  val metricDistances = CircleBuffer(20 km, 40 km, 80 km, 160 km, 320 km).withDefaultValue(80 km)

  val imperialUnits = DisplayUnits("imperial", m_to_kft, m_to_nmi, mps_to_kts, imperialDistances)
  val metricUnits = DisplayUnits("metric", m_to_km, m_to_km, mps_to_kph, metricDistances)

  val displayUnits = CircleBuffer(imperialUnits, metricUnits)

}
