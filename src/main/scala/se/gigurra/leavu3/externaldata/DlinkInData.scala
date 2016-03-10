package se.gigurra.leavu3.externaldata

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Schema, Parsed}
import se.gigurra.leavu3.util.{RestClient, SimpleTimer}
import se.gigurra.serviceutils.json.JSON

/**
  * Created by kjolh on 3/10/2016.
  */
case class DlinkInData(source: SourceData) extends Parsed[DlinkInData.type] {

}

object DlinkInData extends Schema[DlinkInData] {

  def startPoller(fps: Int, addr: String, port: Int): Unit = {

    val client = RestClient(addr, port)

    SimpleTimer.fromFps(fps) {
      ExternalData.dlinkIn = JSON.read(client.pollBlocking("/dlink/in"))
    }
  }
}