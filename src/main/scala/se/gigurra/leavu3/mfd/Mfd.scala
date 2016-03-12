package se.gigurra.leavu3.mfd

import com.badlogic.gdx.math.{Vector2, Vector3}
import se.gigurra.leavu3.externaldata.{DlinkInData, DlinkOutData, GameData}
import se.gigurra.leavu3.gfx.RenderContext._

case class Mfd() {

  def update(game: GameData, dlinkIn: DlinkInData, dlinkOut: DlinkOutData): Unit = frame {


    val p = self.position : Vector2

    geo_viewport(at = p, viewportSize = 20.0f, heading = self.heading).ppi {

      batched {

        val ul = p + (-5f,  5f)
        val ur = p + ( 5f,  5f)
        val ll = p + (-5f, -5f)
        val lr = p + ( 5f, -5f)

        circle(at = p, radius = 10f, color = WHITE)
        circle(at = p, radius = 5f, typ = FILL)
        circle(at = p, radius = 2.5f, typ = FILL, color = BLACK)
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
