package se.gigurra.leavu3.util

import java.time.Instant

/**
  * Created by kjolh on 3/13/2016.
  */
object CurTime {
  def seconds: Double = Instant.now.toEpochMilli.toDouble / 1000.0
}
