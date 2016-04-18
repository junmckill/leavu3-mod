package se.gigurra.leavu3.util

import com.twitter.util.{Duration, JavaTimer, Timer}

/**
  * Created by kjolh on 3/27/2016.
  */
object DefaultTimer extends UtilTimer

case class UtilTimer(underlying: Timer = new JavaTimer(isDaemon = true)) {

  def fps(fps: Int)(op: => Unit): Unit = {
    require(fps > 0, "Must run with at least 1 fps!")
    underlying.schedule(Duration.fromMilliseconds(1000 / fps))(op)
  }

  def onceAfter(delay: Duration)(f: => Unit) = {
    underlying.doLater(delay)(f)
  }
}