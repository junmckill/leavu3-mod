package se.gigurra.leavu3.mfd

import se.gigurra.leavu3.externaldata.{DlinkInData, DlinkOutData, GameData, Vec2}
import se.gigurra.leavu3.gfx.RenderContext._

case class Mfd() {

  def update(game: GameData, dlinkIn: DlinkInData, dlinkOut: DlinkOutData): Unit = frame {


    val p = self.position : Vec2

    ppi_viewport(viewportSize = 20.0f, heading = self.heading) {


      game.aiWingmen.headOption foreach { wingman =>
        println(wingman)
      }
    }

    ppi_viewport(viewportSize = 20.0f, heading = self.heading) {

      batched {

        val ul: Vec2 = (-5.0,  5.0)
        val ur: Vec2 = ( 5.0,  5.0)
        val ll: Vec2 = (-5.0, -5.0)
        val lr: Vec2 = ( 5.0, -5.0)

        circle(radius = 10f, color = WHITE)
        circle(radius = 5f, typ = FILL)
        circle(radius = 2.5f, typ = FILL, color = BLACK)
        lines(Seq(ul -> ur, ll -> lr), color = GREEN)

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
