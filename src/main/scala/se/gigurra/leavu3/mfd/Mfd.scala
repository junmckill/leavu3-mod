package se.gigurra.leavu3.mfd

import se.gigurra.leavu3.externaldata.{DlinkInData, DlinkOutData, GameData}
import se.gigurra.leavu3.gfx.RenderContext._

case class Mfd() {

  def update(game: GameData, dlinkIn: DlinkInData, dlinkOut: DlinkOutData): Unit = frame {


    geo_viewport(at = self.position, viewportSize = 20, heading = self.heading).ppi {
      batched {

        val ul = self.position + (-5f,  5f)
        val ur = self.position + ( 5f,  5f)
        val ll = self.position + (-5f, -5f)
        val lr = self.position + ( 5f, -5f)

        circle(at = self.position, radius = 10f, color = WHITE)
        circle(at = self.position, radius = 5f, typ = FILL)
        circle(at = self.position, radius = 2.5f, typ = FILL, color = BLACK)
        lines(Seq(ul -> ur, ll -> lr), color = GREEN)
      }
    }

    batched {
      val text =
        s"""
           |Heading: ${self.heading}
           |Velocity: ${self.velocity}
           |nwps: ${game.route.waypoints.size}""".stripMargin

      transform(_.scalexy(1.0f / text.width)) {
        text.draw()
      }
    }


  }

}
