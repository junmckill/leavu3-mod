package se.gigurra.leavu3.mfd

import se.gigurra.leavu3.Configuration
import se.gigurra.leavu3.externaldata._
import se.gigurra.leavu3.gfx.RenderContext._
import se.gigurra.leavu3.util.CircleBuffer

import scala.language.postfixOps

/**
  * Created by kjolh on 3/12/2016.
  */
case class HsdPage(implicit config: Configuration) extends Page {

  val distance = CircleBuffer(10 nmi, 20 nmi, 40 nmi, 80 nmi, 160 nmi).withDefaultValue(40 nmi)
  val deprFactor = CircleBuffer(0.0, 0.5).withDefaultValue(0.5)

  def update(game: GameData, dlinkIn: DlinkInData, dlinkOut: DlinkOutData): Unit = {
    ppi_viewport(viewportSize = distance * 2.0, offs = Vec2(0.0, -distance * deprFactor), heading = self.heading) {
      drawSelf(game)
      drawHsi(game)
      drawWaypoints(game)
      drawAiWingmen(game)
      drawDlinkWingmen(dlinkIn)
      drawDlinkWingmenTargets(dlinkIn)
      drawLockedTargets(game)
    }
    drawMenuItems(game)
  }

  def drawHsi(game: GameData): Unit = {
    circle(radius = distance     * 0.50, color = DARK_GRAY)
    circle(radius = distance     * 1.00)
    circle(radius = distance     * 1.50)
    lines(shapes.hsi.flag * symbolScale + Vec2(0.0, distance * 0.50),
      shapes.hsi.flag * symbolScale + Vec2(0.0, distance * 1.00),
      shapes.hsi.flag * symbolScale + Vec2(0.0, distance * 1.50),
      shapes.hsi.eastPin * symbolScale + Vec2(distance * 0.50, 0.0),
      shapes.hsi.eastPin * symbolScale + Vec2(distance * 1.00, 0.0),
      shapes.hsi.eastPin * symbolScale + Vec2(distance * 1.50, 0.0),
      shapes.hsi.westPin * symbolScale + Vec2(-distance * 0.50, 0.0),
      shapes.hsi.westPin * symbolScale + Vec2(-distance * 1.00, 0.0),
      shapes.hsi.westPin * symbolScale + Vec2(-distance * 1.50, 0.0),
      shapes.hsi.southPin * symbolScale + Vec2(0.0, -distance * 0.50),
      shapes.hsi.southPin * symbolScale + Vec2(0.0, -distance * 1.00),
      shapes.hsi.southPin * symbolScale + Vec2(0.0, -distance * 1.50)
    )
  }

  def drawSelf(game: GameData): Unit = {
    transform(_.rotate(-self.heading)) {
      lines(shapes.self.coords * symbolScale, LIGHT_GRAY)
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
        text.drawNextToTarget(wp.position, WHITE)
      }
    }
  }

  def drawAiWingmen(game: GameData): Unit = {
    game.aiWingmen foreach { wingman =>
      circle(at = wingman.position - self.position, radius = 0.025 * symbolScale, color = CYAN)
    }
  }

  def drawDlinkWingmen(dlinkIn: DlinkInData): Unit = {
  }

  def drawDlinkWingmenTargets(dlinkIn: DlinkInData): Unit = {
  }

  def drawLockedTargets(game: GameData): Unit = {
  }

  def drawMenuItems(game: GameData): Unit = {

    /*
    batched {
      val text =
        s"""
           |Heading: ${self.heading}
           |Velocity: ${self.velocity}
           |nwps: ${game.route.waypoints.size}""".stripMargin

      transform(_.scalexy(1.5f / text.width)) {
        text.draw(color = RED)
      }
    }*/
  }
}
