package se.gigurra.leavu3.externaldata

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Schema, Parsed}
import se.gigurra.leavu3.util.{RestClient, SimpleTimer}
import se.gigurra.serviceutils.json.JSON

/**
  * Created by kjolh on 3/10/2016.
  */
case class GameData(source: SourceData) extends Parsed[GameData.type] {

}

object GameData extends Schema[GameData] {

  val path = "export/dcs_remote_export_data()"

  def startPoller(fps: Int, addr: String, port: Int): Unit = {

    val client = RestClient(addr, port)

    SimpleTimer.fromFps(fps) {
      ExternalData.gameData = JSON.read(
        client.getBlocking(path, cacheMaxAgeMillis = Some((1000.0 / fps / 2.0).toLong))
      )

    }
  }

}