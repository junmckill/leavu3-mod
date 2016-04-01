package se.gigurra.leavu3.mfd

import com.badlogic.gdx.graphics.Color
import se.gigurra.leavu3.datamodel._
import se.gigurra.leavu3.gfx.RenderContext._
import se.gigurra.leavu3.gfx.{PpiProjection, Projection, ScreenProjection}
import se.gigurra.leavu3.interfaces.{GameIn, MouseClick}
import se.gigurra.leavu3.util.{CircleBuffer, CurTime}
import se.gigurra.serviceutils.twitter.logging.Logging

import scala.language.postfixOps

abstract class Page(val name: String)(implicit config: Configuration) extends Logging {

  val stdTextSize = 0.75f
  val ppiProjection = new PpiProjection
  val screenProjection = new ScreenProjection
  val displayUnits = DisplayUnits.displayUnits.setBy(_.name == config.initialUnits)
  val shortName = this.getClass.getSimpleName.toLowerCase.subSequence(0, 3)

  var wingmenTgtsLastTminus1 = Seq.empty[Vec3] // To calculate ai wingmen tgt headings
  var wingmenTgtsLastTminus2 = Seq.empty[Vec3] // To calculate ai wingmen tgt headings

  logger.info(s"Created $shortName mfd page")

  def distScale: CircleBuffer[Double] = displayUnits.distScale

  def mouseClicked(click: MouseClick): Unit =  {}

  def pressOsb(i: Int): Unit = {}

  def draw(game: GameData, dlinkIn: Seq[(String, DlinkData)]): Unit


  //////////////////////////////////////////////////////////////////////////////
  // Common symbols

  protected def scanZoneAzDirectionAndWidth: (Double, Double) = {
    val game = GameIn.snapshot
    val sensors = game.sensors.status
    val sttScanZoneOverride = game.pdt.isDefined &&
      (game.aircraftMode.isInCac || game.aircraftMode.isStt)
    val width = if (sttScanZoneOverride) 2.5f else sensors.scanZone.size.azimuth
    val direction = if (sttScanZoneOverride) game.pdt.get.bearing else self.heading + sensors.scanZone.direction.azimuth
    (direction, width)
  }

  protected def drawWpByIndex[_: Projection](wp: Waypoint,
                                             selected: Boolean = false): Unit = {

    val text = if (wp.index > 0) (wp.index - 1).toString else "x"
    drawWp(wp, Some(text), selected)
  }

  protected def drawWp[_: Projection](wp: Waypoint,
                                     text: Option[String],
                                     selected: Boolean = false): Unit ={
    at(wp.position) {
      circle(radius = 0.015 * symbolScale, typ = if (selected) FILL else LINE, color = WHITE)

      text foreach { text =>
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
                                           centerText: String = "",
                                           rightText: String = "",
                                           fill: Boolean = false,
                                           drawAlt: Boolean = true): Unit = {
    val radius = 0.015 * symbolScale

    at(position, heading.getOrElse(0.0)) {
      circle(radius = radius, color = color, typ = if (fill) FILL else LINE)
      if (heading.isDefined)
        lines(Seq(Vec2(0.0, radius) -> Vec2(0.0, radius * 3)))
    }

    at(position) {
      val altText = (position.z * displayUnits.m_to_altUnit).round.toString
      altText.drawLeftOf(scale = stdTextSize, color = color)
      centerText.drawCentered(scale = stdTextSize * 0.50f, color = if (fill) BLACK else color)
      rightText.drawRightOf(scale = stdTextSize * 0.75f, color = color)
    }
  }

  protected def drawJammer[_: Projection](position_actual: Vec3,
                                          heading: Option[Double],
                                          color: Color,
                                          centerText: String = "",
                                          rightText: String = "",
                                          designated: Boolean = false,
                                          drawDistUndesignated: Double): Unit = {


    val drawDist = if (designated) {
      GameIn.snapshot.tdcPosition.fold(drawDistUndesignated)(tdcp => (tdcp - self.position).asBra.range2d)
    } else {
      drawDistUndesignated
    }

    val radius = 0.015 * symbolScale
    val dir = (position_actual - self.position).normalized
    val position = self.position + dir * drawDist

    def doDraw(): Unit = rect(radius * 2.0, radius * 2.0, typ = if (designated) FILL else LINE, color = color)

    at(position, self.heading) {
      doDraw()
      rotatedTo(45.0f) {
        doDraw()
      }
    }

    at(position) {
      val bra = (position - self.position).asBra
      val elevation = math.asin(bra.deltaAltitude/bra.range2d).toDegrees.round
      val altText = (if (elevation > 0) "+" else "") + elevation.toString
      altText.drawLeftOf(scale = stdTextSize * 0.75f, color = color)
      centerText.drawCentered(scale = stdTextSize * 0.50f, color = if (designated) BLACK else color)
      rightText.drawRightOf(scale = stdTextSize * 0.75f, color = color)
    }
  }

  protected def drawAiWingmen[_: Projection](game: GameData): Unit = {
    for (wingman <- game.aiWingmen) {
      drawContact(wingman.position, Some(wingman.heading), CYAN, centerText = "AI")
    }
  }

  protected def drawAiWingmenTargets[_: Projection](game: GameData): Unit = {

    // Hack in heading of tgts if possible
    val shouldDrawHeading =
      game.aiWingmenTgts.size == wingmenTgtsLastTminus1.size &&
        game.aiWingmenTgts.size == wingmenTgtsLastTminus2.size

    for ((tgtPos, i) <- game.aiWingmenTgts.zipWithIndex) {
      if (shouldDrawHeading) {
        val tMinus1 = wingmenTgtsLastTminus1(i)
        val tMinus2 = wingmenTgtsLastTminus2(i)
        val delta = tMinus1 - tMinus2
        val heading = math.atan2(delta.x, delta.y).toDegrees
        drawContact(tgtPos, Some(heading), RED, rightText = "ai")
      } else {
        drawContact(tgtPos, None, RED, rightText = "ai")
      }
    }

    if (wingmenTgtsLastTminus1 != game.aiWingmenTgts) {
      wingmenTgtsLastTminus2 = wingmenTgtsLastTminus1
      wingmenTgtsLastTminus1 = game.aiWingmenTgts
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

  protected def drawDlinkMarks[_: Projection](dlinkIn: Seq[(String, DlinkData)]): Unit = {
    for {
      (name, member) <- dlinkIn
      (id, mark) <- member.marks
    } {
      drawDlinkMark(name, member, id, mark)
    }
  }

  protected def drawDlinkMembersAndTargets[_: Projection](dlinkIn: Seq[(String, DlinkData)]): Unit = {

    val dlinksOfInterest = dlinkIn.filter(m => m._2.data.planeId != self.planeId || m._1 != self.dlinkCallsign)

    for ((name, member) <- dlinksOfInterest) {

      val lag = CurTime.seconds - member.timestamp
      val memberPosition = member.position + member.velocity * lag

      drawContact(memberPosition, Some(member.heading), CYAN, centerText = name.take(2))

      for (target <- member.targets.reverse) { // draw lowest index (=highest prio) last

        val targetPosition = target.position + target.velocity * lag

        if (target.isPositionKnown) {
          drawContact(
            position = targetPosition,
            heading = Some(target.heading),
            color = contactColor(target, fromDatalink = true),
            rightText = name.take(2),
            fill = true
          )
        } else { // HOJ
          lineBetween(memberPosition, targetPosition, YELLOW, scaleOut = 10000.0)
        }
      }

    }

  }
}
