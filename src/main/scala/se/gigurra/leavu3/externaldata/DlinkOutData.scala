package se.gigurra.leavu3.externaldata

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Schema, Parsed}
import se.gigurra.leavu3.util.{RestClient, SimpleTimer}
import se.gigurra.serviceutils.json.JSON

/**
  * Created by kjolh on 3/10/2016.
  */
case class DlinkOutData(source: SourceData) extends Parsed[DlinkOutData.type] {

}

object DlinkOutData extends Schema[DlinkOutData] {

  def startPoller(fps: Int, addr: String, port: Int): Unit = {

    val client = RestClient(addr, port)

    SimpleTimer.fromFps(fps) {
      ExternalData.dlinkOut = JSON.read(client.getBlocking("/dlink/out", cacheMaxAgeMillis = Some(10000L)))
    }
  }
}