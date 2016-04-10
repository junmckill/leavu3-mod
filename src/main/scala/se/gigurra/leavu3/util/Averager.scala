package se.gigurra.leavu3.util

import scala.language.implicitConversions

/**
  * Created by kjolh on 4/11/2016.
  */
case class Averager(overSeconds: Double = 1.0, initValue: Double = 0.0) {

  private var tLastUpdate = 0.0
  private var value = initValue

  def update(newValue: Double): Unit = {
    val weightNewValue = math.min(1.0, (CurTime.seconds - tLastUpdate) / overSeconds)
    val weightOldValue = 1.0 - weightNewValue
    value = weightNewValue * newValue + weightOldValue * value
    tLastUpdate = CurTime.seconds
  }

  def get: Double = value

}

object Averager {
  implicit def a2d(averager: Averager): Double = averager.get
}
