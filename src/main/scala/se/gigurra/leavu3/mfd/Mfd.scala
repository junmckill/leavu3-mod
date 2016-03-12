package se.gigurra.leavu3.mfd

import com.badlogic.gdx.math.Vector3
import se.gigurra.leavu3.externaldata.{DlinkInData, DlinkOutData, GameData}
import se.gigurra.leavu3.gfx.RenderContext._

case class Mfd() {

  def update(game: GameData, dlinkIn: DlinkInData, dlinkOut: DlinkOutData): Unit = frame {

    val text =
      s"""
         |Heading: ${game.flightModel.trueHeading}
         |Velocity: ${game.flightModel.velocity}
         |nwps: ${game.route.waypoints.size}""".stripMargin

    transform(_.scalexy(1.0f / text.width)) {
      text.draw()


      geo_viewport(at = self.position, viewportSize = 20, heading = self.heading).ppi {
        circle(at = self.position, radius = 10f)
        //circle(radius = 0.1f, filled = false).atScreen(x,y)
      }
    }
  }

}
