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

  def centerOffset = -scale * deprFactor

  def update(game: GameData, dlinkIn: DlinkInData, dlinkOut: DlinkOutData): Unit = {


    ppi_viewport(viewportSize = scale, offs = Vec2(0.0, centerOffset), heading = self.heading) {

      // Draw myself in the center
      circle(radius = 0.05 nmi, color = WHITE, typ = FILL)

      // Draw every ai wingman
      game.aiWingmen foreach { wingman =>
        circle(at = wingman.position - self.position, radius = 0.1 nmi, color = CYAN)
      }
    }

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
