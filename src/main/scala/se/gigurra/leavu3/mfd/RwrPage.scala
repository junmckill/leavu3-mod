package se.gigurra.leavu3.mfd

import se.gigurra.leavu3.DlinkData
import se.gigurra.leavu3.externaldata.GameData

import com.badlogic.gdx.graphics.Color
import se.gigurra.leavu3.externaldata._
import se.gigurra.leavu3.gfx.PpiProjection
import se.gigurra.leavu3.gfx.RenderContext._
import se.gigurra.leavu3.util.{CurTime, CircleBuffer}
import se.gigurra.leavu3.{DlinkSettings, Configuration, DlinkData}

import scala.collection.mutable
import scala.language.postfixOps

/**
  * Created by kjolh on 3/12/2016.
  */
case class RwrPage(implicit config: Configuration, dlinkSettings: DlinkSettings) extends Page("RWR") {
  implicit val projection = new PpiProjection
  val stdTextSize = 0.75f
  val distance = 100 nmi

  override def pressOsb(i: Int): Unit = {
    i match {
      case _ => // Nothing yet
    }
  }

  override def draw(game: GameData, dlinkIn: Map[String, DlinkData]): Unit = {
    viewport(viewportSize = distance * 2.0, offs = Vec2(0.0, 0.0), heading = self.heading) {
      drawSelf(game)
      drawHsi(game)
    }
    drawMenuItems(game)
  }

  def drawHsi(game: GameData): Unit = {
    transform(_.scalexy(0.75f)) {
      circle(radius = distance * 1.00, color = DARK_GRAY)
      lines(
        shapes.hsi.flag * symbolScale + Vec2(0.0, distance * 1.00),
        shapes.hsi.eastPin * symbolScale + Vec2(distance * 1.00, 0.0),
        shapes.hsi.westPin * symbolScale + Vec2(-distance * 1.00, 0.0),
        shapes.hsi.southPin * symbolScale + Vec2(0.0, -distance * 1.00)
      )
    }
  }

  def drawSelf(game: GameData): Unit = {
    transform(_.rotate(-self.heading)) {
      lines(shapes.self.coords * symbolScale, CYAN)
      circle(0.005 * symbolScale, color = CYAN, typ = FILL)
    }
  }

  def drawWaypoints(game: GameData): Unit = {

    def wpByIndex(i: Int): Option[Waypoint] = {
      game.route.waypoints.find(_.index == i)
    }

    for (wp <- game.route.waypoints) {
      circle(at = wp.position - self.position, radius = 0.015 * symbolScale, color = WHITE)
      wpByIndex(wp.next) match {
        case None =>
        case Some(nextWp) =>
          val thisOne = wp.position - self.position
          val nextOne = nextWp.position - self.position
          lines(Seq(thisOne -> nextOne))
      }
    }
    circle(at = game.route.currentWaypoint.position - self.position, radius = 0.015 * symbolScale, typ = FILL, color = WHITE)

    batched {
      for (wp <- game.route.waypoints) {
        val text = if (wp.index > 0) (wp.index - 1).toString else "x"
        at(wp.position) {
          text.drawRightOf(scale = stdTextSize, color = WHITE)
        }
      }
    }
  }

  def drawMenuItems(game: GameData): Unit = {
  }
}
