package se.gigurra.leavu3.lmath

import scala.language.implicitConversions

trait UnitConversions {
  val nmi_to_km = 1.85200
  val km_to_nmi = 1.0 / nmi_to_km
  val m_to_ft = 3.2808399
  val ft_to_m = 1.0 / m_to_ft
  val m_to_kft = 3.2808399 / 1000.0
  val kft_to_m = 1.0 / m_to_kft
  val nmi_to_m = 1.85200 * 1000.0
  val m_to_nmi = 1.0 / nmi_to_m
  val m_to_km = 1.0 / 1000.0
  val km_to_m = 1.0 / m_to_km
  val mps_to_kts = m_to_nmi * 3600.0
  val mps_to_kph = m_to_km * 3600.0
  val kts_to_mps = 1.0 / mps_to_kts
  val kph_to_mps = 1.0 / mps_to_kph

  implicit class MeterValues[T: Numeric](x: T) {
    def d = implicitly[Numeric[T]].toDouble(x)
    def nmi: Double = d * nmi_to_m
    def km: Double = d * km_to_m
  }
}
