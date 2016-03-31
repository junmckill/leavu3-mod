package se.gigurra.leavu3.interfaces

import com.twitter.finagle.FailedFastException
import com.twitter.util.Future
import se.gigurra.leavu3.datamodel.{Configuration, Contact, GameData, Waypoint}
import se.gigurra.leavu3.gfx.Drawable
import se.gigurra.leavu3.util.{ContactMemory, DefaultTimer, IdenticalRequestPending, Resource2String}
import se.gigurra.serviceutils.json.JSON
import se.gigurra.serviceutils.twitter.logging.Logging
import se.gigurra.serviceutils.twitter.service.ServiceException

import scala.collection.mutable

/**
  * Created by kjolh on 3/20/2016.
  */
object GameIn extends Logging {

  val path = "export/dcs_remote_export_data()"

  @volatile var snapshot = GameData()
  @volatile var dcsRemoteConnected = true
  // App doesnt even start otherwise!
  @volatile var dcsGameConnected = false

  def start(appCfg: Configuration, drawable: Drawable): Unit = {
    ScriptInject.start(appCfg)
    Updater.start(appCfg, drawable)
  }

  object Updater {
    def start(appCfg: Configuration, drawable: Drawable): Unit = {
      val fps = appCfg.gameDataFps
      val maxAge = (1000.0 / fps.toDouble / 2.0).toLong

      DefaultTimer.fps(fps) {
        DcsRemote
          .get(path, Some(maxAge))
          .map(JSON.read[GameData])
          .map(postProcess)
          .map { newSnapshot =>
            snapshot = newSnapshot
            dcsRemoteConnected = true
            dcsGameConnected = true
            drawable.draw()
          }.onFailure {
          case e: IdenticalRequestPending => // Ignore
          case e: ServiceException =>
            logger.warning(s"Dcs Remote replied: Could not fetch game data from Dcs Remote: $e")
            dcsRemoteConnected = true
            dcsGameConnected = false
            snapshot = GameData()
          case e: FailedFastException =>
            dcsRemoteConnected = false
            dcsGameConnected = false
            snapshot = GameData()
          case e =>
            logger.error(s"Could not fetch game data from Dcs Remote: $e")
            dcsRemoteConnected = false
            dcsGameConnected = false
            snapshot = GameData()
        }
      }
    }
  }

  private val knownNavWaypoints = new mutable.HashMap[Int, Waypoint]
  private val rwsMemory = ContactMemory()

  def rwsContactNews(contact: Contact): Option[Double] = {
    rwsMemory.get(contact).map(_.news)
  }

  private def postProcess(newData: GameData): GameData = {
    // TODO: Fuse with old data where required
    // Known states that needs fusion:
    // - rws detections (only visible a few frames) - Needs manual storage
    // - If in NAV mode: Store waypoints that have not yet been seen (workaround for missing waypoints bug)
    newData.waypointWorkaround().rwsMemoryWorkaround()
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

    def rwsMemoryWorkaround(): GameData = {
      val rwsContactsKnown = rwsMemory.update(newData.sensors.targets.detected.filterNot(_.isDesignated).filter(_.isPositionKnown))
      newData.withRwsMemory(rwsContactsKnown.map(_.t))
    }

  }

  object ScriptInject extends Logging {

    val luaDataExportScript = Resource2String("lua_scripts/LoDataExport.lua")

    def start(appCfg: Configuration): Unit = {

      DefaultTimer.seconds(1) {
        if (dcsGameConnected && !snapshot.isValid) {
          DcsRemote.get(GameIn.path).map(JSON.read[GameData]).flatMap {
            case BadGameData() =>
              logger.info(s"Injecting data export script .. -> ${GameIn.path}")
              DcsRemote.post("export")(luaDataExportScript)
            case _ =>
              Future.Unit // Good to go!
          }.onFailure {
            case e: IdenticalRequestPending => // Ignore ..
            case e: FailedFastException => // Ignore ..
            case e: ServiceException => logger.warning(s"Dcs Remote replied: Unable to inject game export script: $e")
            case e => logger.error(e, s"Unable to inject export script")
          }
        }
      }
    }

    object BadGameData {
      def unapply(data: GameData): Boolean = data.err.isDefined
    }

  }

}
