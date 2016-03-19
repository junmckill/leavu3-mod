package se.gigurra.leavu3.util

import java.time.Instant

/**
  * Created by kjolh on 3/13/2016.
  */
object CurTime {

  def isEven: Boolean = seconds.round % 2 == 0

  def seconds: Double = Instant.now.toEpochMilli.toDouble / 1000.0
}
