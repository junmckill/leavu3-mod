package se.gigurra.leavu3.math

import scala.language.implicitConversions

trait UnitConversions {
  val nmi_to_km = 1.85200
  val km_to_nmi = 1.0 / nmi_to_km
  val m_to_ft = 3.2808399
  val ft_to_m = 1.0 / m_to_ft
  val m_to_kft = 3.2808399 / 1000.0
  val kft_to_m = 1.0 / m_to_kft
  val nmi_to_m = 1.85200 * 1000.0
  val m_to_nmi = 1 / nmi_to_m

  implicit class MeterValues[T: Numeric](x: T) {
    def d = implicitly[Numeric[T]].toDouble(x)
    def nmi: Double = d * nmi_to_m
    def ft: Double = d * ft_to_m
    def km: Double = d * 1000.0
  }
}
