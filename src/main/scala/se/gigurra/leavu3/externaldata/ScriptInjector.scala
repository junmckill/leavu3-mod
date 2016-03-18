package se.gigurra.leavu3.externaldata

import com.twitter.finagle.FailedFastException
import com.twitter.util.Duration
import se.gigurra.leavu3.util.{Resource2String, SimpleTimer, RestClient}
import se.gigurra.serviceutils.json.JSON
import se.gigurra.serviceutils.twitter.logging.Logging
import se.gigurra.serviceutils.twitter.service.ServiceException

import scala.util.{Failure, Try, Success}

/**
  * Created by kjolh on 3/11/2016.
  */
object ScriptInjector extends Logging {

  val luaDataExportScript = Resource2String("lua_scripts/LoDataExport.lua")

  def startInjecting(addr: String, port: Int): Unit = {

    val client = RestClient(addr, port)

    SimpleTimer.apply(Duration.fromSeconds(10)) {
      Try(JSON.read[GameData](client.getBlocking(GameData.path))) match {
        case Failure(e: FailedFastException) => // Ignore ..
        case Failure(e: ServiceException) =>
          logger.warning(s"Dcs Remote replied: Unable to inject game export script: $e")
        case Failure(_) | Success(BadGameData()) =>
          logger.info(s"Injecting data export script .. -> ${GameData.path}")
          client.postBlocking("export", luaDataExportScript)
        case _ =>
          // It's already loaded
      }
    }
  }
}

object BadGameData {
  def unapply(data: GameData): Boolean = data.err.isDefined
}
