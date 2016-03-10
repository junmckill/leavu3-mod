package se.gigurra.leavu3.util

import com.twitter.util.{JavaTimer, Duration}

/**
  * Created by kjolh on 3/10/2016.
  */
class SimpleTimer(interval: Duration, op: () => Unit) {
  private val timer = new JavaTimer(isDaemon = true)
  timer.schedule(interval) {
    op()
  }
}

object SimpleTimer {

  def apply(interval: Duration)(op: => Unit): SimpleTimer = {
    new SimpleTimer(interval, () => op)
  }

  def fromFps(fps: Int)(op: => Unit): SimpleTimer = {
    apply(Duration.fromMilliseconds(1000 / fps))(op)
  }
}
