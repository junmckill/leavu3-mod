package se.gigurra.leavu3.util

import scala.language.implicitConversions

/**
  * Created by kjolh on 4/11/2016.
  */
case class DerivativeAverager(overSeconds: Double = 1.0,
                              minTimeStep: Double = 0.1,
                              initValue: Double = 0.0) {

  private var tLastUpdate = 0.0
  private var lastValue = initValue
  private var lastDerivative = 0.0

  def update(newValue: Double, t: Double): Unit = {

    val dt = t - tLastUpdate
    val kNew = dt / overSeconds

    if (kNew >= 1.0) {
      lastDerivative = 0.0
      tLastUpdate = t
      lastValue = newValue
    } else if (dt >= minTimeStep) {

      val newDerivative = (newValue - lastValue) / dt
      val kOld = 1.0 - kNew
      lastDerivative = kNew * newDerivative + kOld * lastDerivative
      tLastUpdate = t
      lastValue = newValue
    } else if (kNew < 0.0) {
      tLastUpdate = t
      lastValue = newValue
      lastDerivative = 0.0
    }

  }

  def get: Double = lastDerivative

}

object DerivativeAverager {
  implicit def a2d(averager: Averager): Double = averager.get
}
