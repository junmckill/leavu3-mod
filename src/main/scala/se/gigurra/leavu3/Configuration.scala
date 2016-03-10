package se.gigurra.leavu3

import java.io.FileNotFoundException

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Schema, Parsed}
import se.gigurra.serviceutils.json.JSON
import se.gigurra.serviceutils.twitter.logging.Logging

import scala.util.{Failure, Success, Try}

case class Configuration(source: SourceData = Map.empty) extends Parsed[Configuration.type] {
  val title         = parse(schema.title)
  val x             = parse(schema.x)
  val y             = parse(schema.y)
  val width         = parse(schema.width)
  val height        = parse(schema.height)
  val forceExit     = parse(schema.forceExit)
  val vSyncEnabled  = parse(schema.vSyncEnabled)
  val foregroundFPS = parse(schema.foregroundFPS)
  val backgroundFPS = parse(schema.backgroundFPS)
  val gameDataFps   = parse(schema.gameDataFps)
  val dlinkInFps    = parse(schema.dlinkInFps)
  val dlinkOutFps   = parse(schema.dlinkOutFps)
  val restAddress   = parse(schema.restAddress)
  val restPort      = parse(schema.restPort)
}

object Configuration extends Schema[Configuration] with Logging {
  val title         = required[String]  ("title",         default = "Leavu 3")
  val x             = required[Int]     ("x",             default = 200)
  val y             = required[Int]     ("y",             default = 200)
  val width         = required[Int]     ("width",         default = 1024)
  val height        = required[Int]     ("height",        default = 768)
  val forceExit     = required[Boolean] ("forceExit",     default = false)
  val vSyncEnabled  = required[Boolean] ("vSyncEnabled",  default = true)
  val foregroundFPS = required[Int]     ("foregroundFPS", default = 30)
  val backgroundFPS = required[Int]     ("backgroundFPS", default = 5)
  val gameDataFps   = required[Int]     ("gameDataFps",   default = 40)
  val dlinkInFps    = required[Int]     ("dlinkInFps",    default = 5)
  val dlinkOutFps   = required[Int]     ("dlinkOutFps",   default = 5)
  val restAddress   = required[String]  ("restAddress",   default = "127.0.0.1")
  val restPort      = required[Int]     ("restPort",      default = 12340)


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