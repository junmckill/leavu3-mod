package se.gigurra.leavu3.mfd

import com.badlogic.gdx.graphics.Color
import se.gigurra.leavu3.datamodel.{Bra, Configuration, Contact, DlinkData, GameData, UnitType, Vec2, self}
import se.gigurra.leavu3.gfx.RenderContext._
import se.gigurra.leavu3.gfx.{BScopeProjection, Projection}
import se.gigurra.leavu3.interfaces.GameIn
import se.gigurra.leavu3.lmath.NormalizeDegrees

import scala.collection.mutable

/**
  * Created by kjolh on 3/12/2016.
  */
case class FcrPage(implicit config: Configuration, mfd: MfdIfc) extends Page("FCR") {

  def screenDistMeters: Double = GameIn.snapshot.sensors.status.scale.distance

  def screenWidthDegrees: Double = GameIn.snapshot.sensors.status.scale.azimuth

  val inset = 0.2
  val OSB_AI = 18
  val OSB_DL = 17
  val OSB_BE = 16
  val OSB_ABS = 8
  var shouldDrawAi = true
  var shouldDrawDl = true
  var shouldDrawBe = true
  var shouldDrawAbsBearings = false

  override def pressOsb(i: Int): Unit = {
    i match {
      case OSB_AI => shouldDrawAi = !shouldDrawAi
      case OSB_DL => shouldDrawDl = !shouldDrawDl
      case OSB_BE => shouldDrawBe = !shouldDrawBe
      case OSB_ABS => shouldDrawAbsBearings = !shouldDrawAbsBearings
      case _ => // Nothing yet
    }
  }

  override def draw(game: GameData, dlinkIn: Seq[(String, DlinkData)]): Unit = {
    matchIngameScale(game)
    drawConformal(game, dlinkIn)
    drawInfo(game, dlinkIn)
  }

  def drawConformal(game: GameData, dlinkIn: Seq[(String, DlinkData)]): Unit = {
    implicit val bscopeProjection = BScopeProjection(screenWidthDegrees, config.use3dBscope)
    transform(_.scalexy(1.0 - inset)) {

      viewport(screenDistMeters, self.heading, offs = Vec2(0.0, 0.0)) {
        scissor(at = (0.0, 0.0), size = (screenDistMeters, screenDistMeters)) {
          drawScanZoneUnderlay(game)
          drawGrid(game)
          drawDlz(game)
          if (shouldDrawBe) {
            drawSelectedWaypoint(game)
          }
          if (shouldDrawDl) {
            drawDlinkMarks(dlinkIn)
            if (shouldDrawAi) {
              drawAiWingmen(game)
              drawAiWingmenTargets(game)
            }
            drawDlinkMembersAndTargets(dlinkIn)
          }
          drawOwnContacts(game, dlinkIn)
          drawTdc(game)
          drawScanZoneOverlay(game)
        }
        drawSurroundEdge()
      }
    }
  }

  def braMiddleOfScreen = Bra(self.heading, screenDistMeters * 0.5, 0.0)

  def drawScanZoneUnderlay[_: Projection](game: GameData): Unit = {

    def drawGreyUnderlay(): Unit = {
      at(self.position + braMiddleOfScreen.toOffset: Vec2, heading = self.heading) {
        rect(screenDistMeters, screenDistMeters, typ = FILL, color = DARK_GRAY.scaleAlpha(0.25f))
      }
    }

    def drawScannedArea(): Unit = {
      if (game.sensors.status.sensorOn) {
        val (direction, width) = scanZoneAzDirectionAndWidth

        val braScanCenter = Bra(direction, screenDistMeters * 0.5, 0.0)
        val hCoverage = width / screenWidthDegrees

        at(self.position + braScanCenter.toOffset: Vec2, heading = self.heading) {
          rect(hCoverage * screenDistMeters, screenDistMeters, typ = FILL, color = BLACK)
        }
      }
    }

    drawGreyUnderlay()
    drawScannedArea()
  }

  def drawGrid[_: Projection](game: GameData): Unit = {

    val k = screenDistMeters / 2.0

    at(self.position + braMiddleOfScreen.toOffset: Vec2, heading = self.heading) {
      for (angle <- Seq(0.0, 90.0)) {
        transform(_.rotate(angle)) {
          lines(Seq(
            Vec2(-1.0, 0.0) -> Vec2(1.0, 0.0),
            Vec2(-1.0, 0.5) -> Vec2(1.0, 0.5),
            Vec2(-1.0, -0.5) -> Vec2(1.0, -0.5)
          ) * k, TEAL.scaleRGB(0.35f))
        }
      }
    }
  }

  def drawSelectedWaypoint[_: Projection](game: GameData): Unit = {
    drawWp(game.route.currentWaypoint, None, selected = true)
  }

  def drawOwnContacts[_: Projection](game: GameData, dlinkIn: Seq[(String, DlinkData)]): Unit = {

    val contacts = new mutable.HashMap[Int, Contact] // id -> data
    val order = new mutable.HashMap[Int, Int] // id -> index

    import game.sensors.targets._

    for {
      (collection, isRws) <- Seq((detected, true), (tws.map(_.contact), false), (locked.map(_.contact), false))
      (contact, i) <- collection.zipWithIndex
    } {
      order.put(contact.id, i)
      contacts.put(contact.id, contact)
    }

    val fromOwnRadar = Contact.FromOwnRadar(order)
    import fromOwnRadar._

    val positionsEchoed = contacts.values.toSeq
      .filterNot(_.isDesignated)
      .filter(_.isPositionKnown)
      .sortBy(_.index)

    val positionsDesignated = contacts.values.toSeq
      .filter(_.isDesignated)
      .filter(_.isPositionKnown)
      .sortBy(_.index)

    val bearingsEchoed = contacts.values.toSeq
      .filterNot(_.isDesignated)
      .filterNot(_.isPositionKnown)
      .sortBy(_.index)

    val bearingsDesignated = contacts.values.toSeq
      .filter(_.isDesignated)
      .filterNot(_.isPositionKnown)
      .sortBy(_.index)

    def drawKnownPosContacts(contacts: Seq[Contact]): Unit = {
      for (contact <- contacts.reverse) {
        // draw lowest index (=highest prio) last
        drawOwnContact(contact, fromOwnRadar)
      }
    }

    def drawJammers(contacts: Seq[Contact]): Unit = {
      for (contact <- contacts.reverse) {
        // draw lowest index (=highest prio) last

        val baseColor = YELLOW
        val color = baseColor.scaleAlpha(contact.news)

        if (contact.isDesignated) {
          lineBetween(0.5 * (self.position + contact.position), contact.position, YELLOW, scaleIn = 10000.0, scaleOut = 10000.0)
        }

        drawJammer(
          position_actual = contact.position,
          heading = if (contact.isRws) None else Some(contact.heading),
          color = color,
          centerText = if (contact.isDesignated) (contact.index + 1).toString else "",
          designated = contact.isDesignated,
          drawDistUndesignated = math.min(math.max(30.nmi, screenDistMeters * 0.75), screenDistMeters * 0.95)
        )

      }
    }

    // Separated to ensure draw order
    drawJammers(bearingsEchoed)
    drawJammers(bearingsDesignated)

    // Separated to ensure draw order
    drawKnownPosContacts(positionsEchoed)
    drawKnownPosContacts(positionsDesignated)

  }

  def drawScanZoneOverlay[_: Projection](game: GameData): Unit = {
    // DCS doesn't export momentary antenna position, so don't try to draw that


    def drawHorizontalStuff(): Unit = {
      if (game.sensors.status.sensorOn) {

        val symbolOffs = 0.001

        val (azDir, azWidth) = scanZoneAzDirectionAndWidth
        val azLeftEdge = azDir - azWidth / 2.0 * 0.995
        val azRightEdge = azDir + azWidth / 2.0 * 0.995

        val dist = screenDistMeters * symbolOffs
        val braLeftEdge = Bra(azLeftEdge, dist, 0.0)
        val braRightEdge = Bra(azRightEdge, dist, 0.0)

        game.tdcBra.foreach { tdcBra =>

          val leftPos = self.position + braLeftEdge.toOffset: Vec2
          val rightPos = self.position + braRightEdge.toOffset: Vec2
          val centerPos = self.position + tdcBra.with2dLength(dist).toOffset: Vec2
          val centerPos2x = self.position + tdcBra.with2dLength(screenDistMeters * 0.05).toOffset: Vec2
          val h = 0.05

          for (pos <- Seq(leftPos, centerPos, rightPos)) {
            at(pos, heading = self.heading) {
              lines(Seq(Vec2(0.0, -h) -> Vec2(0.0, h)) * symbolScale)
            }
          }

          at(centerPos2x) {
            bearingString((centerPos2x - self.position).asBra.bearing).drawCentered(WHITE, scale = stdTextSize)
          }
        }
      }
    }

    def drawVerticalStuff(): Unit = {
      if (game.sensors.status.sensorOn) {

        def elev2dist(elev: Double): Double = screenDistMeters / 2.0 + screenDistMeters * elev / 180.0

        val symbolOffs = 0.025

        val (elDir, elWidth) = scanZoneElDirectionAndHeight
        val az = self.heading - (1.0 - symbolOffs) * game.sensors.status.scale.azimuth / 2.0
        val distCenter = elev2dist(elDir)
        val distUp = elev2dist(elDir + elWidth / 2.0)
        val distDown = elev2dist(elDir - elWidth / 2.0)

        val braCenter = Bra(az, distCenter, 0.0)
        val braUp = Bra(az, distUp, 0.0)
        val braDown = Bra(az, distDown, 0.0)

        val center = self.position + braCenter.toOffset: Vec2
        val up = self.position + braUp.toOffset: Vec2
        val down = self.position + braDown.toOffset: Vec2

        val w = 0.0225

        for (pos <- Seq(center, up, down)) {
          at(pos, heading = self.heading) {
            lines(Seq(Vec2(-w, 0.0) -> Vec2(w, 0.0)) * symbolScale)
          }
        }

        at(center) {
          val elevString = deltaAngleString(elDir)
          elevString.drawRightOf(WHITE, 0.5f)
        }

      }
    }

    drawHorizontalStuff()
    drawVerticalStuff()
  }

  def drawSurroundEdge[_: Projection](): Unit = {
    rect(screenDistMeters, screenDistMeters, color = TEAL)
  }

  def drawInfo(game: GameData, dlinkIn: Seq[(String, DlinkData)]): Unit = {
    implicit val p = screenProjection
    drawBearings(game)
    drawElevations(game)
    drawBullsEyeNumbrs(game)
    drawBraNumbrs(game)
    drawScale(game)
    drawModes(game)
    drawTargetInfo(game)
    drawOffText(game)
    drawOsbs(game)
  }

  def drawBearings[_: Projection](game: GameData): Unit = {
    val left = -1.0 + inset
    val right = 1.0 - inset
    val max = game.sensors.status.scale.azimuth / 2.0
    val y = -1.0 + inset * 0.85
    val dx = (right - left) / 4
    val dAngle = max / 2.0

    for (i <- -2 to 2) {
      val di = (i - -2).toDouble
      val x = left + di * dx

      transform(_.translate(x.toFloat, y.toFloat)) {
        if (shouldDrawAbsBearings) {
          val angle = NormalizeDegrees._0360(dAngle * di - max + self.heading)
          angle.round.toString.pad(3, '0').drawCentered(GRAY, 0.60f)
        } else {
          if (i != 0) {
            val angle = deltaAngleString(NormalizeDegrees.pm180(dAngle * di - max))
            angle.drawCentered(GRAY, 0.60f)
          } else {
            val angle = NormalizeDegrees._0360(self.heading)
            angle.round.toString.pad(3, '0').drawCentered(WHITE, 0.75f)
          }
        }
      }
    }
  }

  def drawOsbs[_: Projection](game: GameData): Unit = {
    osb.drawBoxed(OSB_AI, "AI", boxed = shouldDrawAi)
    osb.drawBoxed(OSB_DL, "DL", boxed = shouldDrawDl)
    osb.drawBoxed(OSB_BE, "BE", boxed = shouldDrawBe)
    osb.drawBoxed(OSB_ABS, "ABS", boxed = shouldDrawAbsBearings)
  }


  def drawOffText[_: Projection](game: GameData): Unit = {
    if (game.sensors.status.sensorOff) {
      "SENSOR OFF".drawCentered(WHITE)
    }
  }

  def drawElevations[_: Projection](game: GameData): Unit = {

    val bottom = -1.0 + inset
    val top = 1.0 - inset
    val max = 90.0
    val x = -1.0 + inset * 0.75
    val dy = (top - bottom) / 4
    val dAngle = max / 2.0

    for (i <- -2 to 2) {
      val di = (i - -2).toDouble
      val y = bottom + di * dy

      transform(_.translate(x.toFloat, y.toFloat)) {
        if (math.abs(i) == 1) {
          val angle = deltaAngleString(NormalizeDegrees.pm180(dAngle * di - max))
          angle.drawCentered(GRAY, 0.60f)
        }
      }
    }

  }

  def drawBullsEyeNumbrs[_: Projection](game: GameData): Unit = {
    drawBullsEyeNumbers(game, 0.0175, (-0.95, 0.95))
  }

  def drawBraNumbrs[_: Projection](game: GameData): Unit = {
    drawBraNumbers(game, 0.0175, (0.50, 0.95))
  }

  def drawScale(game: GameData): Unit = {

    implicit val p = screenProjection
    val scale = config.symbolScale * 0.025 / font.getSpaceWidth

    batched {

      transform(_
        .translate(-1.0f + inset.toFloat * 0.4f, 1.0f - 1.2f * inset.toFloat)
        .scalexy(scale)) {

        var n = 0
        def drawTextLine(value: Any, color: Color): Unit = {
          transform(_
            .translate(y = -n.toFloat * font.getLineHeight)
            .scalexy(1.0f)
          ) {
            value.toString.drawRaw(xAlign = 0.5f, color = color)
          }
          n += 1
        }

        drawTextLine(s"${(distScale.current * m_to_distUnit).round.toString.pad(3)}", WHITE)

      }
    }
  }

  def drawModes(game: GameData): Unit = {

    implicit val p = screenProjection
    val sensors = game.sensors.status
    val scale = config.symbolScale * 0.02 / font.getSpaceWidth

    batched {

      transform(_
        .translate(-1.0f + inset.toFloat + 0.05f, 1.0f - inset.toFloat - 0.05f)
        .scalexy(scale)) {

        var n = 0
        def drawTextLine(value: Any, color: Color): Unit = {
          transform(_
            .translate(y = -n.toFloat * font.getLineHeight)
            .scalexy(0.85f)
          ) {
            value.toString.drawRaw(xAlign = 0.5f, color = color)
          }
          n += 1
        }

        drawTextLine(s"${game.aircraftMode.master} / ${game.aircraftMode.submode}", LIGHT_GRAY)
        drawTextLine(s"${sensors.prf.selection} / ${sensors.prf.current}", LIGHT_GRAY)
      }
    }
  }

  def drawDlz[_: Projection](game: GameData): Unit = {

    game.pdt
      .filter(_.isPositionKnown)
      .filter(_.dlz.rAero > 1000)
      .foreach { pdt =>

        val tgtDist = (pdt.position - self.position).norm

        val vector = Bra(self.heading + game.sensors.status.scale.azimuth / 2.0, 1.0, 0.0)
        val braTgt = vector.with2dLength(math.max(1.0, tgtDist))
        val braRMin = vector.with2dLength(math.max(1.0, pdt.dlz.rMin))
        val braRtr = vector.with2dLength(pdt.dlz.rTr)
        val braRPi = vector.with2dLength(pdt.dlz.rPi)
        val braRAero = vector.with2dLength(pdt.dlz.rAero)

        val w = 0.050 * screenDistMeters * config.symbolScale
        val h = 0.0035 * screenDistMeters * config.symbolScale

        at(self.position + braRMin.toOffset, heading = self.heading) {
          rect(w, h, color = TEAL, typ = LINE)
          transform(_.rotate(self.heading)) {
            "rmin ".drawLeftOf(TEAL, scale = 0.5f)
          }
        }

        at(self.position + braRtr.toOffset, heading = self.heading) {
          rect(w, h, color = TEAL, typ = FILL)
          transform(_.rotate(self.heading)) {
            " rtr ".drawLeftOf(TEAL, scale = 0.5f)
          }
        }

        at(self.position + braRPi.toOffset, heading = self.heading) {
          rect(w, h, color = TEAL, typ = FILL)
          transform(_.rotate(self.heading)) {
            " rpi ".drawLeftOf(TEAL, scale = 0.5f)
          }
        }

        at(self.position + braRAero.toOffset, heading = self.heading) {
          rect(w, h / 2.0, color = TEAL, typ = FILL)
        }

        at(self.position + braTgt.toOffset, heading = self.heading) {

          val tgtClosureText = closureTextOf(pdt)
          val color = contactColor(pdt, fromDatalink = false)

          rect(w, h / 2.0, color = contactColor(pdt, fromDatalink = false), typ = FILL)
          transform(_.rotate(self.heading)) {
           s" $tgtClosureText ".drawLeftOf(color, scale = 0.5f)
          }
        }
      }
  }

  def drawTargetInfo[_: Projection](game: GameData): Unit = {

    implicit val p = screenProjection

    val textScale = config.symbolScale * 0.0175 / font.getSpaceWidth

    val ownSpeed = self.velocity.norm * mps_to_speedUnit
    val ownSpeedText = s"self: ${ownSpeed.round} -> ${headingString(self.heading)}"

    var n = 0
    def drawTextLine(value: Any, color: Color): Unit = {
      transform(_
        .scalexy(textScale)
        .translate(y = -n.toFloat * font.getLineHeight)
      ) {
        value.toString.drawRaw(xAlign = 0.5f, color = color)
      }
      n += 1
    }

    at((-0.20, 0.925)) {

      drawTextLine(ownSpeedText, CYAN)

      game.pdt.filter(_.isPositionKnown).foreach { pdt =>
        val tgtSpeed = pdt.velocity.norm * mps_to_speedUnit
        val tgtClosureText = closureTextOf(pdt)
        val tgtSpeedText = s" pdt: ${tgtSpeed.round} -> ${headingString(pdt.heading)} c$tgtClosureText"
        val unitTypeString = createUnitTypeString(pdt)
        drawTextLine(tgtSpeedText, contactColor(pdt, fromDatalink = false))
        drawTextLine(s"      $unitTypeString", contactColor(pdt, fromDatalink = false))
      }

    }
  }

  def closureOf(target: Contact): Double = {
    val vnToTgt = (target.position - self.position).normalized
    val vnFromTgt = -vnToTgt
    val tgtClosure = ((target.velocity dot vnFromTgt) + (self.velocity dot vnToTgt)) * mps_to_speedUnit
    tgtClosure
  }

  def closureTextOf(target: Contact): String = {
    deltaText(closureOf(target))
  }

  def createUnitTypeString(contact: Contact): String = {

    if ((contact.position - self.position).norm < 25.nmi) {

      val fullName = contact.typ.fullName.toUpperCase
      val firstWord = fullName.split(' ').headOption.getOrElse("")
      val iDashP1 = firstWord.indexOf('-') + 1
      if (iDashP1 >= 0 && iDashP1 < firstWord.length) {
        val (start, rightOfDash) = firstWord.splitAt(iDashP1)
        val iNotNumber = rightOfDash.indexWhere(!_.isDigit)
        if (iNotNumber >= 0) {
          firstWord.substring(0, start.length + iNotNumber)
        } else {
          firstWord
        }
      } else {
        firstWord
      }
    } else {
      "UKN"
    }
  }

}
