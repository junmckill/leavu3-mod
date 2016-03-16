package se.gigurra.leavu3

import java.io.FileNotFoundException

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Schema, Parsed}
import se.gigurra.serviceutils.json.JSON
import se.gigurra.serviceutils.twitter.logging.Logging

import scala.util.{Failure, Success, Try}

case class Configuration(source: SourceData = Map.empty) extends Parsed[Configuration.type] {
  val title             = parse(schema.title)
  val x                 = parse(schema.x)
  val y                 = parse(schema.y)
  val width             = parse(schema.width)
  val height            = parse(schema.height)
  val forceExit         = parse(schema.forceExit)
  val vSyncEnabled      = parse(schema.vSyncEnabled)
  val foregroundFPS     = parse(schema.foregroundFPS)
  val backgroundFPS     = parse(schema.backgroundFPS)
  val gameDataFps       = parse(schema.gameDataFps)
  val dcsRemoteAddress  = parse(schema.dcsRemoteAddress)
  val dcsRemotePort     = parse(schema.dcsRemotePort)
  val aaSamples         = parse(schema.aaSamples)
  val instrument        = parse(schema.instrument)
  val symbolScale       = parse(schema.symbolScale)
  val relayDlink        = parse(schema.relayDlink)
  val borderless        = parse(schema.borderless)
}

object Configuration extends Schema[Configuration] with Logging {
  val title             = required[String]  ("title",             default = "Leavu 3")
  val x                 = required[Int]     ("x",                 default = 200)
  val y                 = required[Int]     ("y",                 default = 200)
  val width             = required[Int]     ("width",             default = 1024)
  val height            = required[Int]     ("height",            default = 1024)
  val forceExit         = required[Boolean] ("forceExit",         default = false)
  val vSyncEnabled      = required[Boolean] ("vSyncEnabled",      default = true)
  val foregroundFPS     = required[Int]     ("foregroundFPS",     default = 30)
  val backgroundFPS     = required[Int]     ("backgroundFPS",     default = 5)
  val gameDataFps       = required[Int]     ("gameDataFps",       default = 40)
  val dcsRemoteAddress  = required[String]  ("dcsRemoteAddress",  default = "127.0.0.1")
  val dcsRemotePort     = required[Int]     ("dcsRemotePort",     default = 12340)
  val aaSamples         = required[Int]     ("aaSamples",         default = 4)
  val instrument        = required[String]  ("instrument",        default = "se.gigurra.leavu3.mfd.Mfd")
  val symbolScale       = required[Double]  ("symbolScale",       default = 1.0)
  val relayDlink        = required[Boolean] ("relayDlink",        default = true)
  val borderless        = required[Boolean] ("borderless",        default = false)

  def readFromFile(s: String = "leavu3-cfg.json"): Configuration = {
    logger.info(s"Loading configuration file: $s")
    Try(JSON.read[Configuration](scala.io.Source.fromFile(s).mkString)) match {
      case Success(cfg) => cfg
      case Failure(e: FileNotFoundException) =>
        logger.info(s"No config file found (path=$s) - using default configuration")
        Configuration()
      case Failure(e) =>
        logger.fatal(e, s"Failed to read config file ($e) '$s'")
        throw e
    }
  }
}

case class DlinkSettings(source: SourceData = Map.empty) extends Parsed[DlinkSettings.type] {
  val host         = parse(schema.host)
  val port         = parse(schema.port)
  val team         = parse(schema.team)
  val callsign     = parse(schema.callsign)
  val inFps        = parse(schema.inFps)
  val outFps       = parse(schema.outFps)
}

object DlinkSettings extends Schema[DlinkSettings] {
  val host         = required[String]  ("host",         default = "build.culvertsoft.se")
  val port         = required[Int]     ("port",         default = 12340)
  val team         = required[String]  ("team",         default = "BLUE_RABBITS")
  val callsign     = required[String]  ("callsign",     default = "JarJar")
  val inFps        = required[Int]     ("inFps",        default = 1)
  val outFps       = required[Int]     ("outFps",       default = 2)
}