package se.gigurra.leavu3.mfd

import se.gigurra.leavu3.externaldata.{DlinkInData, DlinkOutData, GameData, Vec2}
import se.gigurra.leavu3.gfx.RenderContext._
import se.gigurra.leavu3.util.CircleBuffer
import scala.language.postfixOps

/**
  * Created by kjolh on 3/12/2016.
  */
case class HsdPage() extends Page {

  val scale = CircleBuffer(10 nmi, 20 nmi, 40 nmi, 80 nmi, 160 nmi).withDefaultValue(40 nmi)
  val deprFactor = CircleBuffer(0.0, 0.25).withDefaultValue(0.25)

  def standardObjectRadius = 0.025 * toScreenCoords

  def update(game: GameData, dlinkIn: DlinkInData, dlinkOut: DlinkOutData): Unit = {
    ppi_viewport(viewportSize = scale, offs = Vec2(0.0, -scale * deprFactor), heading = self.heading) {
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
  }

  def drawSelf(game: GameData): Unit = {
    circle(radius = standardObjectRadius, color = WHITE, typ = FILL)
  }

  def drawWaypoints(game: GameData): Unit = {
  }

  def drawAiWingmen(game: GameData): Unit = {
    game.aiWingmen foreach { wingman =>
      circle(at = wingman.position - self.position, radius = standardObjectRadius, color = CYAN)
    }
  }

  def drawDlinkWingmen(dlinkIn: DlinkInData): Unit = {
  }

  def drawDlinkWingmenTargets(dlinkIn: DlinkInData): Unit = {
  }

  def drawLockedTargets(game: GameData): Unit = {
  }

  def drawMenuItems(game: GameData): Unit = {

    batched {
      val text =
        s"""
           |Heading: ${self.heading}
           |Velocity: ${self.velocity}
           |nwps: ${game.route.waypoints.size}""".stripMargin

      transform(_.scalexy(1.5f / text.width)) {
        text.draw()
      }
    }
  }
}
