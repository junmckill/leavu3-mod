package se.gigurra.leavu3.interfaces

import com.twitter.finagle.FailedFastException
import com.twitter.util.Duration
import se.gigurra.leavu3.datamodel.Waypoint
import se.gigurra.leavu3.datamodel.{Configuration, GameData}
import se.gigurra.leavu3.gfx.Drawable
import se.gigurra.leavu3.util.{Resource2String, SimpleTimer}
import se.gigurra.serviceutils.json.JSON
import se.gigurra.serviceutils.twitter.logging.Logging
import se.gigurra.serviceutils.twitter.service.ServiceException

import scala.collection.mutable
import scala.util.{Failure, Success, Try}

/**
  * Created by kjolh on 3/20/2016.
  */
object GameIn extends Logging {

  val path = "export/dcs_remote_export_data()"

  @volatile var snapshot = GameData()
  @volatile var dcsRemoteConnected = true // App doesnt even start otherwise!
  @volatile var dcsGameConnected = false

  def start(appCfg: Configuration, drawable: Drawable): Unit = {
    ScriptInject.start(appCfg)
    Updater.start(appCfg, drawable)
  }

  object Updater {
    def start(appCfg: Configuration, drawable: Drawable): Unit = {
      val fps = appCfg.gameDataFps

      SimpleTimer.fromFps(fps) {
        Try(DcsRemote.getBlocking(path, cacheMaxAgeMillis = Some((1000.0 / fps.toDouble / 2.0).toLong))) match {
          case Success(stringData) =>
            val newData = JSON.read[GameData](stringData)
            snapshot = process(newData)
            dcsRemoteConnected = true
            dcsGameConnected = true
            drawable.draw()
          case Failure(e: ServiceException) =>
            logger.warning(s"Dcs Remote replied: Could not fetch game data from Dcs Remote: $e")
            dcsRemoteConnected = true
            dcsGameConnected = false
            snapshot = GameData()
          case Failure(e: FailedFastException) =>
            dcsRemoteConnected = false
            dcsGameConnected = false
            snapshot = GameData()
          // Ignore ..
          case Failure(e) =>
            logger.error(s"Could not fetch game data from Dcs Remote: $e")
            dcsRemoteConnected = false
            dcsGameConnected = false
            snapshot = GameData()
        }
      }
    }
  }

  private val knownNavWaypoints = new mutable.HashMap[Int, Waypoint]

  private def process(newData: GameData): GameData = {
    // TODO: Fuse with old data where required
    // Known states that needs fusion:
    // - rws detections (only visible a few frames) - Needs manual storage
    // - If in NAV mode: Store waypoints that have not yet been seen (workaround for missing waypoints bug)
    newData.waypointWorkaround()
  }

  implicit class GameDataWorkarounds(newData: GameData) {

    def waypointWorkaround(): GameData = {
      if (newData.route.waypoints.nonEmpty) {
        newData
      } else {
        // DCS FC waypoint export in NAV mode is broken.. we only get the current one.
        // so keep in memory which ones we've cycled through (yay - Nice idea by GG!)
        val curWp = newData.route.currentWaypoint
        knownNavWaypoints.get(curWp.index) match {
          case Some(knownWaypoint) if knownWaypoint != curWp =>
            logger.info(s"Known navpoint changed - assuming mission change - clearing known navpoints!")
            knownNavWaypoints.clear()
          case _ =>
          // No clearing required. Same or new waypoint
        }

        // DCS gives us an extra wp (index 2 million :))
        if (0 <= curWp.index && curWp.index < 100)
          knownNavWaypoints.put(curWp.index, curWp)
        else
          knownNavWaypoints.put(-1, curWp.withIndex(-1))

        val allWps = knownNavWaypoints.values.toSeq.sortBy(_.index)
        val newRoute = newData.route.withWaypoints(allWps)
        newData.withRoute(newRoute)
      }
    }

  }

  object ScriptInject extends Logging {

    val luaDataExportScript = Resource2String("lua_scripts/LoDataExport.lua")

    def start(appCfg: Configuration): Unit = {

      DcsRemote.getBlocking("123")

      SimpleTimer.apply(Duration.fromSeconds(10)) {
        Try(JSON.read[GameData](DcsRemote.getBlocking(GameIn.path))) match {
          case Failure(e: FailedFastException) => // Ignore ..
          case Failure(e: ServiceException) =>
            logger.warning(s"Dcs Remote replied: Unable to inject game export script: $e")
          case Failure(_) | Success(BadGameData()) =>
            logger.info(s"Injecting data export script .. -> ${GameIn.path}")
            DcsRemote.postBlocking("export", luaDataExportScript)
          case _ =>
          // It's already loaded
        }
      }
    }

    object BadGameData {
      def unapply(data: GameData): Boolean = data.err.isDefined
    }

  }
}
