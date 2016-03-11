package se.gigurra.leavu3.externaldata

import java.util.NoSuchElementException

import com.twitter.util.Duration
import se.gigurra.leavu3.util.{Resource2String, SimpleTimer, RestClient}
import se.gigurra.serviceutils.twitter.logging.Logging

import scala.util.{Failure, Try, Success}

/**
  * Created by kjolh on 3/11/2016.
  */
object ScriptInjector extends Logging {

  val luaDataExportScript = Resource2String("lua_scripts/LoDataExport.lua")

  def startInjecting(addr: String, port: Int): Unit = {
    val client = RestClient(addr, port)
    SimpleTimer.apply(Duration.fromSeconds(10)) {
      Try(client.getBlocking(GameData.path)) match {
        case Success(_) => // It's already loaded
        case Failure(_: NoSuchElementException) =>
          logger.info(s"Injecting data export script .. -> ${GameData.path}")
          client.postBlocking("export", luaDataExportScript)
        case Failure(e) =>
          logger.error(s"Unable to inject export script: $e", e)
      }

    }
  }
}
