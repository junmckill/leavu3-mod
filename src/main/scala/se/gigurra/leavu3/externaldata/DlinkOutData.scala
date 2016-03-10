package se.gigurra.leavu3.externaldata

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Schema, Parsed}

/**
  * Created by kjolh on 3/10/2016.
  */
case class DlinkOutData(source: SourceData) extends Parsed[DlinkOutData.type] {

}

object DlinkOutData extends Schema[DlinkOutData] {

  def startPoller(fps: Int): Unit = {

  }
}