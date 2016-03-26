package se.gigurra.leavu3.util

import com.twitter.util.{Duration, JavaTimer, Time}

/**
  * Created by kjolh on 3/27/2016.
  */
object DefaultTimer {

  val underlying = new JavaTimer(isDaemon = true)

  def seconds(interval: Int)(op: => Unit): Unit = {
    underlying.schedule(Time.now, Duration.fromSeconds(interval))(op)
  }

  def fps(fps: Int)(op: => Unit): Unit = {
    underlying.schedule(Duration.fromMilliseconds(1000 / fps))(op)
  }
}

