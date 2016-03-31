package se.gigurra.leavu3.mfd

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack
import se.gigurra.leavu3.datamodel.{Configuration, DlinkData, GameData, Vec2, Vec3, self}
import se.gigurra.leavu3.gfx.{BScopeProjection, Projection}
import se.gigurra.leavu3.interfaces.GameIn
import se.gigurra.leavu3.gfx.RenderContext._

/**
  * Created by kjolh on 3/12/2016.
  */
case class FcrPage(implicit config: Configuration) extends Page("FCR") {

  def screenDistMeters: Double = GameIn.snapshot.sensors.status.scale.distance
  def screenWidthDegrees: Double = GameIn.snapshot.sensors.status.scale.azimuth
  val inset = 0.2

  def draw(game: GameData, dlinkIn: Seq[(String, DlinkData)]): Unit = {
    drawConformal(game, dlinkIn)
  }

  def drawConformal(game: GameData, dlinkIn: Seq[(String, DlinkData)]): Unit = {
    implicit val bscopeProjection = BScopeProjection(screenWidthDegrees, config.use3dBscope)
    transform(_.scalexy(1.0 - inset)) {

      viewport(screenDistMeters, self.heading, offs = Vec2(0.0, 0.0)) {
        scissor(at = (0.0, 0.0), size = (screenDistMeters, screenDistMeters)) {
          drawContacts(game, dlinkIn)
        }
        drawSurroundEdge()
      }
    }
  }

  def drawContacts[_: Projection](game: GameData, dlinkIn: Seq[(String, DlinkData)]): Unit = {
    drawContact(self.position + Vec3(0.0, screenDistMeters/2, 0.0), heading = None, color = RED)
  }

  def drawSurroundEdge[_: Projection](): Unit = {
    rect(screenDistMeters, screenDistMeters, color = WHITE)
  }

}
