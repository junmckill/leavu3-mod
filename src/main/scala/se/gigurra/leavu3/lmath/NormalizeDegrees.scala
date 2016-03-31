package se.gigurra.leavu3.lmath

object NormalizeDegrees {

  def _0360(v: Double): Double = {
    val whole360s = v.toLong / 360
    val rest = v - whole360s.toDouble * 360.0
    if (rest < 0.0) {
      rest + 360.0
    } else {
      rest
    }
  }

  def pm180(v: Double): Double = {
    val z360 = _0360(v)
    if (z360 > 180.0) {
      z360 - 360.0
    } else {
      z360
    }
  }

}
