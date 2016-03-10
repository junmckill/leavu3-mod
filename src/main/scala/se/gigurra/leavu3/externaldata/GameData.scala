package se.gigurra.leavu3.externaldata

import com.twitter.util.Duration
import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Schema, Parsed}
import se.gigurra.leavu3.util.SimpleTimer

/**
  * Created by kjolh on 3/10/2016.
  */
case class GameData(source: SourceData) extends Parsed[GameData.type] {

}

object GameData extends Schema[GameData] {

  def startPoller(fps: Int): Unit = {
    SimpleTimer(Duration.fromMilliseconds(1000 / fps)) {

    }
  }
}