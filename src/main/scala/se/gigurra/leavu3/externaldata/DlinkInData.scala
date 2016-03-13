package se.gigurra.leavu3.externaldata

import se.gigurra.leavu3.{Configuration, DlinkData}
import se.gigurra.leavu3.util.{RestClient, SimpleTimer}
import se.gigurra.serviceutils.json.JSON

object DlinkInData {

  def startPoller(config: Configuration): Unit = {

    val client = RestClient(config.dlinkHost, config.dlinkPort)

    SimpleTimer.fromFps(config.dlinkInFps) {
      val rawData = JSON.readMap(client.getBlocking(config.dlinkTeam, cacheMaxAgeMillis = Some(10000L)))
      rawData.map {
        case (id, raw) => ???
      }
      ???
    }
  }
}