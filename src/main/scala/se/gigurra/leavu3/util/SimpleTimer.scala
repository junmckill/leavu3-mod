package se.gigurra.leavu3.util

import com.twitter.util.{Time, JavaTimer, Duration}

/**
  * Created by kjolh on 3/10/2016.
  */
class SimpleTimer(interval: Duration, op: () => Unit) {
  SimpleTimer.timer.schedule(Time.now, interval) {
    op()
  }
}

object SimpleTimer {

  private val timer = new JavaTimer(isDaemon = true)

  def apply(interval: Duration)(op: => Unit): SimpleTimer = {
    new SimpleTimer(interval, () => op)
  }

  def fromFps(fps: Int)(op: => Unit): SimpleTimer = {
    apply(Duration.fromMilliseconds(1000 / fps))(op)
  }
}
