package se.gigurra.leavu3.externaldata

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Schema, Parsed}

/**
  * Created by kjolh on 3/10/2016.
  */
case class DlinkInData(source: SourceData) extends Parsed[DlinkInData.type] {

}

object DlinkInData extends Schema[DlinkInData] {

  def startPoller(fps: Int): Unit = {

  }
}