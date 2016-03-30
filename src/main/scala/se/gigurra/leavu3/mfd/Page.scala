package se.gigurra.leavu3.mfd

import com.badlogic.gdx.graphics.Color
import se.gigurra.leavu3.datamodel._
import se.gigurra.leavu3.gfx.RenderContext._
import se.gigurra.leavu3.gfx.{PpiProjection, Projection, ScreenProjection}
import se.gigurra.leavu3.interfaces.MouseClick
import se.gigurra.leavu3.util.CircleBuffer
import se.gigurra.serviceutils.twitter.logging.Logging

abstract class Page(val name: String)(implicit config: Configuration) extends Logging {

  val stdTextSize = 0.75f
  val ppiProjection = new PpiProjection
  val screenProjection = new ScreenProjection
  val displayUnits = DisplayUnits.displayUnits.setBy(_.name == config.initialUnits)
  val shortName = this.getClass.getSimpleName.toLowerCase.subSequence(0, 3)
  logger.info(s"Created $shortName mfd page")

  def distScale: CircleBuffer[Double] = displayUnits.distScale

  def mouseClicked(click: MouseClick): Unit =  {}

  def pressOsb(i: Int): Unit = {}

  def draw(game: GameData, dlinkIn: Seq[(String, DlinkData)]): Unit


  //////////////////////////////////////////////////////////////////////////////
  // Common symbols

  protected def drawWpByIndex[_: Projection](wp: Waypoint,
                                             selected: Boolean = false): Unit = {

    val text = if (wp.index > 0) (wp.index - 1).toString else "x"
    drawWp(wp, Some(text), selected)
  }

  protected def drawWp[_: Projection](wp: Waypoint,
                                     text: Option[String],
                                     selected: Boolean = false): Unit ={

    circle(at = wp.position - self.position, radius = 0.015 * symbolScale, typ = if (selected) FILL else LINE, color = WHITE)

    text foreach { text =>
      at(wp.position) {
        text.drawRightOf(scale = stdTextSize, color = WHITE)
      }
    }
  }

  protected def drawSelf[_: Projection](surroundCircleRadius: Double): Unit = {
    transform(_.rotate(-self.heading)) {
      circle(surroundCircleRadius, color = DARK_GRAY, typ = LINE)
      lines(shapes.self.coords * symbolScale, CYAN)
      circle(0.005 * symbolScale, color = CYAN, typ = FILL)
    }
  }

  protected def drawHsi[_: Projection](close :Boolean,
                                       middle: Boolean,
                                       far: Boolean,
                                       tics: Boolean): Unit = {
    if (close) {
      circle(radius = distScale * 0.50, color = DARK_GRAY)
      lines(shapes.hsi.flag * symbolScale + Vec2(0.0, distScale * 0.50),
        shapes.hsi.eastPin * symbolScale + Vec2(distScale * 0.50, 0.0),
        shapes.hsi.westPin * symbolScale + Vec2(-distScale * 0.50, 0.0),
        shapes.hsi.southPin * symbolScale + Vec2(0.0, -distScale * 0.50)
      )
    }

    if (middle) {
      circle(radius = distScale * 1.00, color = DARK_GRAY)
      lines(
        shapes.hsi.flag * symbolScale + Vec2(0.0, distScale * 1.00),
        shapes.hsi.eastPin * symbolScale + Vec2(distScale * 1.00, 0.0),
        shapes.hsi.westPin * symbolScale + Vec2(-distScale * 1.00, 0.0),
        shapes.hsi.southPin * symbolScale + Vec2(0.0, -distScale * 1.00)
      )
    }

    if (far) {
      circle(radius = distScale * 1.50, color = DARK_GRAY)
      lines(
        shapes.hsi.flag * symbolScale + Vec2(0.0, distScale * 1.50),
        shapes.hsi.eastPin * symbolScale + Vec2(distScale * 1.50, 0.0),
        shapes.hsi.westPin * symbolScale + Vec2(-distScale * 1.50, 0.0),
        shapes.hsi.southPin * symbolScale + Vec2(0.0, -distScale * 1.50)
      )
    }

    if (tics) {
      lines(shapes.hsi.detail(distScale.toFloat), DARK_GRAY)
    }
  }

  protected def contactColor(contact: Contact, fromDatalink: Boolean): Color = {
    if (self.coalition == contact.country) {
      GREEN
    } else if (fromDatalink) {
      RED
    } else {
      YELLOW
    }
  }

  protected def drawTdc[_: Projection](game: GameData): Unit = {
    game.tdcPosition foreach { tdc =>
      at(tdc, self.heading) {
        val d = 0.02
        lines(Seq(
          Vec2(-d, -d) -> Vec2(-d, d),
          Vec2( d, -d) -> Vec2( d, d)
        ) * symbolScale, WHITE)
      }

      at(tdc) {
        val coverage = game.sensors.status.scanZone.altitudeCoverage
        val elevationText = game.sensors.status.scanZone.direction.elevation.round.toString
        val coverageText =
          s"""${(coverage.max * displayUnits.m_to_altUnit).round}
             |${(coverage.min * displayUnits.m_to_altUnit).round}""".stripMargin
        coverageText.drawRightOf(scale = 0.5f, color = WHITE)
        elevationText.drawLeftOf(scale = 0.5f, color = WHITE)
      }
    }
  }

  protected def drawContact[_: Projection](position: Vec3,
                                           heading: Option[Double],
                                           color: Color,
                                           idText: String = "",
                                           textToTheSide: Boolean = true,
                                           fill: Boolean = false): Unit = {
    val radius = 0.015 * symbolScale

    at(position, heading.getOrElse(0.0)) {
      circle(radius = radius, color = color, typ = if (fill) FILL else LINE)
      if (heading.isDefined)
        lines(Seq(Vec2(0.0, radius) -> Vec2(0.0, radius * 3)))
    }

    at(position) {
      val altText = (position.z * displayUnits.m_to_altUnit).round.toString
      altText.drawLeftOf(scale = stdTextSize, color = color)
      if (textToTheSide) {
        idText.drawRightOf(scale = stdTextSize * 0.75f, color = color)
      } else {
        idText.drawCentered(scale = stdTextSize * 0.50f, color = if (fill) BLACK else color)
      }
    }
  }

  protected def drawDlinkMark[_: Projection](name: String, member: Member, id: String, mark: Mark): Unit = {
    val radius = 0.015 * symbolScale
    at(mark.position) {
      circle(radius = radius, typ = LINE, color = YELLOW)
      circle(radius = radius * 0.5f, typ = LINE, color = YELLOW)
      mark.id.drawRightOf(scale = stdTextSize * 0.8f, color = YELLOW)
    }
  }

}
