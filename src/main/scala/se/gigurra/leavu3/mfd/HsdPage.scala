package se.gigurra.leavu3.mfd

import com.badlogic.gdx.graphics.Color
import se.gigurra.leavu3.datamodel._
import se.gigurra.leavu3.gfx.RenderContext._
import se.gigurra.leavu3.util.{CircleBuffer, CurTime}
import se.gigurra.leavu3.interfaces.{Dlink, MouseClick}

import scala.collection.mutable
import scala.language.postfixOps

/**
  * Created by kjolh on 3/12/2016.
  */
case class HsdPage(implicit config: Configuration) extends Page("HSD") {

  var shouldMatchIngameScale = true
  var shouldDrawDetailedHsi = true
  var shouldDrawOwnHeading = true
  val deprFactor = CircleBuffer(0.0, 0.5).withDefaultValue(0.5)

  val OSB_DEPR = 1
  val OSB_HDG = 2
  val OSB_SCALE = 17
  val OSB_HSI = 3
  val OSB_DEL = 7
  val OSB_UNITS = 9

  override def pressOsb(i: Int): Unit = {
    i match {
      case OSB_DEPR => deprFactor.stepDown()
      case OSB_HSI => shouldDrawDetailedHsi = !shouldDrawDetailedHsi
      case OSB_HDG => shouldDrawOwnHeading = !shouldDrawOwnHeading
      case OSB_DEL => Dlink.Out.deleteMark(Dlink.config.callsign)
      case OSB_UNITS => displayUnits.stepUp()
      case _ => // Nothing yet
    }
  }

  override def mouseClicked(click: MouseClick): Unit =  {
    val screenCenter = Vec2(0.0, -deprFactor)
    val offs = (click.ortho11 - screenCenter) * distScale
    val relativeBra = offs.asBra
    val bra = relativeBra.copy(bearingRaw = self.heading + relativeBra.bearing)
    val clickPos = self.position + bra.toOffset
    Dlink.Out.addMark(Mark(Dlink.config.callsign, clickPos))
  }

  override def draw(game: GameData, dlinkIn: Map[String, DlinkData]): Unit = {
    matchIngameScale(game)
    viewport(viewportSize = distScale * 2.0, offs = Vec2(0.0, -distScale * deprFactor), heading = self.heading){
      implicit val p = ppiProjection
      drawSelf(game)
      drawHsi(game)
      drawWayPoints(game)
      drawScanZone(game)
      drawAiWingmen(game)
      drawAiWingmenTargets(game)
      drawDlinkMembersAndTargets(dlinkIn)
      drawDlinkMarks(dlinkIn)
      drawLockedTargets(game)
      drawTdc(game)
    }(ppiProjection)
    drawBullsEyeNumbers(game)
    drawBraNumbers(game)
    drawOwnHeading(game)
    drawModes(game)
    drawOsbs(game)
  }

  def matchIngameScale(game: GameData) = {
    if (shouldMatchIngameScale) {
      val x = distScale.items.minBy(x => math.abs(x - game.sensors.status.scale.distance))
      distScale.set(x)
    }
  }

  def drawHsi(game: GameData): Unit = {
    drawHsi(close = true, middle = true, far = true, tics = shouldDrawDetailedHsi)(ppiProjection)
  }

  def drawSelf(game: GameData): Unit = {
    drawSelf(0.0)(ppiProjection)
  }

  def drawWayPoints(game: GameData): Unit = {
    implicit val p = ppiProjection

    def wpByIndex(i: Int): Option[Waypoint] = {
      game.route.waypoints.find(_.index == i)
    }

    for (wp <- game.route.waypoints) {
      wpByIndex(wp.next) foreach { nextWp =>
        val thisOne = wp.position - self.position
        val nextOne = nextWp.position - self.position
        lines(Seq(thisOne -> nextOne), WHITE)
      }
      drawWpByIndex(wp, wp.index == game.route.currentWaypoint.index)
    }
  }

  def drawScanZone(game: GameData): Unit = {
    implicit val p = ppiProjection
    val sensors = game.sensors.status
    if (sensors.sensorOn) {
      val sttScanZoneOverride = game.pdt.isDefined &&
        (game.aircraftMode.isInCac || game.aircraftMode.isStt)
      val dist = sensors.scale.distance
      val width = if (sttScanZoneOverride) 2.5f else sensors.scanZone.size.azimuth
      val direction = if (sttScanZoneOverride) game.pdt.get.bearing else self.heading + sensors.scanZone.direction.azimuth
      arc(radius = dist, angle = width, direction = direction, color = LIGHT_GRAY)
    }
  }

  def drawAiWingmen(game: GameData): Unit = {
    implicit val p = ppiProjection
    for (wingman <- game.aiWingmen) {
      val radius = 0.015 * symbolScale
      at(wingman.position, wingman.heading) {
        circle(radius = radius, color = CYAN)
        lines(Seq(Vec2(0.0, radius) -> Vec2(0.0, radius * 3)))
      }
    }

    batched {
      for (wingman <- game.aiWingmen) {
        at(wingman.position) {
          val altText = (wingman.position.z * displayUnits.m_to_altUnit).round.toString
          altText.drawLeftOf(scale = stdTextSize, color = CYAN)
          val nameText = "AI"
          nameText.drawRightOf(scale = stdTextSize * 0.8f, color = CYAN)
        }
      }
    }
  }

  var wingmenTgtsLastTminus1 = Seq.empty[Vec3]
  var wingmenTgtsLastTminus2 = Seq.empty[Vec3]

  def drawAiWingmenTargets(game: GameData): Unit = {
    implicit val p = ppiProjection
    val radius = 0.015 * symbolScale

    // Hack in heading of tgts if possible
    val shouldDrawHeading =
      game.aiWingmenTgts.size == wingmenTgtsLastTminus1.size &&
      game.aiWingmenTgts.size == wingmenTgtsLastTminus2.size

    for ((tgtPos, i) <- game.aiWingmenTgts.zipWithIndex) {
      at(tgtPos) {
        circle(radius = radius, color = RED)
        if (shouldDrawHeading) {
          val tMinus1 = wingmenTgtsLastTminus1(i)
          val tMinus2 = wingmenTgtsLastTminus2(i)
          val delta = tMinus1 - tMinus2
          val heading = math.atan2(delta.x, delta.y).toDegrees
          rotatedTo(heading) {
            lines(Seq(Vec2(0.0, radius) -> Vec2(0.0, radius * 3)))
          }
        }
      }
    }

    batched {
      for (tgtPos <- game.aiWingmenTgts) {
        at(tgtPos) {
          val text = (tgtPos.z * displayUnits.m_to_altUnit).round.toString
          text.drawLeftOf(scale = stdTextSize, color = RED)
        }
      }
    }

    if (wingmenTgtsLastTminus1 != game.aiWingmenTgts) {
      wingmenTgtsLastTminus2 = wingmenTgtsLastTminus1
      wingmenTgtsLastTminus1 = game.aiWingmenTgts
    }
  }


  def drawDlinkMarks(dlinkIn: Map[String, DlinkData]): Unit = {
    implicit val p = ppiProjection

    val radius = 0.015 * symbolScale

    def draw(f: (String, Member, String, Mark) => Unit): Unit = {
      for {
        (name, member) <- dlinkIn
        (id, mark) <- member.marks
      } {
        at(mark.position) {
          f(name, member, id, mark)
        }
      }
    }

    draw { (name, member, id, mark) =>
      circle(radius = radius, typ = LINE, color = YELLOW)
      circle(radius = radius * 0.5f, typ = LINE, color = YELLOW)
    }

    draw { (name, member, id, mark) =>
      batched {
        mark.id.drawRightOf(scale = stdTextSize * 0.8f, color = YELLOW)
      }
    }

  }

  def drawDlinkMembersAndTargets(dlinkIn: Map[String, DlinkData]): Unit = {
    implicit val p = ppiProjection

    val dlinksOfInterest = dlinkIn.filter(m => m._2.data.planeId != self.planeId || m._1 != self.dlinkCallsign)

    for ((name, member) <- dlinksOfInterest) {

      val lag = CurTime.seconds - member.timestamp
      val memberPosition = member.position + member.velocity * lag
      val radius = 0.015 * symbolScale

      at(memberPosition, member.heading) {
        circle(radius = radius, typ = FILL, color = CYAN)
        lines(Seq(Vec2(0.0, radius) -> Vec2(0.0, radius * 3)))
      }

      batched {
        at(memberPosition) {
          val altText = (memberPosition.z * displayUnits.m_to_altUnit).round.toString
          altText.drawLeftOf(scale = stdTextSize, color = CYAN)
          val nameText = name.take(2)
          nameText.drawRightOf(scale = stdTextSize, color = CYAN)
        }
      }

      for (target <- member.targets) {

        val targetPosition = target.position + target.velocity * lag

        if (target.isPositionKnown) {
          at(targetPosition, heading = target.heading) {
            circle(radius = radius, typ = FILL, color = contactColor(target, fromDatalink = true))
            lines(Seq(Vec2(0.0, radius) -> Vec2(0.0, radius * 3)))
          }
          at(targetPosition) {
            batched {
              val altText = (targetPosition.z * displayUnits.m_to_altUnit).round.toString
              altText.drawLeftOf(scale = stdTextSize, color = contactColor(target, fromDatalink = true))
              val nameText = name.take(2)
              nameText.drawRightOf(scale = stdTextSize, color = contactColor(target, fromDatalink = true))
            }
          }
        } else at(memberPosition) { // HOJ
          val b = targetPosition - member.position: Vec2
          lines(Seq(Vec2() -> b) * 10000.0, RED)
        }
      }

    }

  }

  def drawLockedTargets(game: GameData): Unit = {
    implicit val p = ppiProjection

    val radius = 0.020 * symbolScale

    val contacts = new mutable.HashMap[Int, Contact] // id -> data
    val order = new mutable.HashMap[Int, Int] // id -> index

    implicit class ContactWithIndex(c: Contact) {
      def index: Int = order(c.id)
    }

    import game.sensors.targets._
    for {
      collection <- Seq(detected, tws.map(_.contact), locked.map(_.contact))
      (contact, i) <- collection.zipWithIndex
    } {
      order.put(contact.id, i)
      contacts.put(contact.id, contact)
    }

    val positionsDesignated = contacts.values.toSeq
      .filter(_.isDesignated)
      .filter(_.isPositionKnown)
      .sortBy(_.index)

    val bearingsDesignated = contacts.values.toSeq
      .filter(_.isDesignated)
      .filterNot(_.isPositionKnown)
      .sortBy(_.index)

    for (contact <- positionsDesignated) {
      at(contact.position, heading = contact.heading) {
        circle(radius = radius, typ = FILL, color = contactColor(contact, fromDatalink = false))
        lines(Seq(Vec2(0.0, radius) -> Vec2(0.0, radius * 3)))
      }
    }

    for (contact <- bearingsDesignated) {
      val offs = contact.position - self.position : Vec2
      lines(Seq(Vec2() -> offs) * 10000.0, YELLOW)
    }

    batched {
      for (contact <- positionsDesignated) {
        at(contact.position) {
          val text = (contact.position.z * displayUnits.m_to_altUnit).round.toString
          text.drawLeftOf(scale = stdTextSize, color = contactColor(contact, fromDatalink = false))
          (contact.index + 1).toString.drawCentered(scale = stdTextSize, color = BLACK)
        }
      }
    }

  }

  def drawBullsEyeNumbers(game: GameData) = {
    implicit val p = screenProjection

    def mkBraString(prefix: String, bra: Bra): String = s"$prefix : ${bra.brString(displayUnits.m_to_distUnit)}"

    val bullsEye = game.route.currentWaypoint
    val selfBra = (self.position - bullsEye.position).asBra
    val scale = config.symbolScale * 0.02 / font.getSpaceWidth

    batched { atScreen(-0.9, 0.9) {

      transform(_
        .scalexy(scale)) {

        val beStr = s"bullseye : wp ${bullsEye.index - 1}"
        val selfStr = mkBraString("self".pad(8), selfBra)

        var n = 0
        def drawTextLine(str: String, color: Color): Unit = {
          transform(_.translate(y = -n.toFloat * font.getLineHeight))(str.drawRaw(xAlign = 0.5f, color = color))
          n += 1
        }

        drawTextLine(beStr, LIGHT_GRAY)
        drawTextLine(selfStr, CYAN)

        game.tdcPosition foreach { tdc =>
          val tdcBra = (tdc - bullsEye.position).asBra
          val tdcStr = mkBraString("tdc".pad(8), tdcBra)
          drawTextLine(tdcStr, WHITE)
        }

        game.pdt.filter(_.isPositionKnown) foreach { pdt =>
          val pdtBra = (pdt.position - bullsEye.position).asBra
          val pdtStr = mkBraString("pdt".pad(8), pdtBra)
          drawTextLine(pdtStr, contactColor(pdt, fromDatalink = false))
        }

      }
    }}

  }

  def drawBraNumbers(game: GameData) = {
    implicit val p = screenProjection

    def mkBraString(prefix: String, bra: Bra): String = s"$prefix : ${bra.brString(displayUnits.m_to_distUnit)}"

    val scale = config.symbolScale * 0.02 / font.getSpaceWidth

    batched { atScreen(0.45, 0.9) {

      transform(_
        .scalexy(scale)) {

        val beStr = s" BR from : self"

        var n = 0
        def drawTextLine(str: String, color: Color): Unit = {
          transform(_.translate(y = -n.toFloat * font.getLineHeight))(str.drawRaw(xAlign = 0.5f, color = color))
          n += 1
        }

        drawTextLine(beStr, LIGHT_GRAY)

        val wp = game.route.currentWaypoint
        val wpBra = (wp.position - self.position).asBra
        val wpStr = mkBraString(s"wp ${wp.index-1}".pad(8), wpBra)
        drawTextLine(wpStr, DARK_GRAY)

        game.tdcPosition foreach { tdc =>
          val tdcBra = (tdc - self.position).asBra
          val tdcStr = mkBraString("tdc".pad(8), tdcBra)
          drawTextLine(tdcStr, WHITE)
        }

        game.pdt.filter(_.isPositionKnown) foreach { pdt =>
          val pdtBra = (pdt.position - self.position).asBra
          val pdtStr = mkBraString("pdt".pad(8), pdtBra)
          drawTextLine(pdtStr, contactColor(pdt, fromDatalink = false))
        }

      }
    }}

  }

  def drawOsbs(game: GameData): Unit = {
    implicit val p = screenProjection
    import Mfd.Osb._
    drawBoxed(OSB_DEPR, "DEP", boxed = deprFactor.index != 0)
    drawBoxed(OSB_HDG, "HDG", boxed = shouldDrawOwnHeading)
    drawBoxed(OSB_HSI, "HSI", boxed = shouldDrawDetailedHsi)
    if (Dlink.Out.hasMark(Dlink.config.callsign))
      drawBoxed(OSB_DEL, "DEL", boxed = false)
    drawBoxed(OSB_SCALE, (distScale.get * displayUnits.m_to_distUnit).round.toString, boxed = false)
    Mfd.Osb.draw(OSB_UNITS, displayUnits.name.toUpperCase.take(3))
  }

  def drawOwnHeading(game: GameData): Unit = {
    implicit val p = screenProjection
    if (shouldDrawOwnHeading) {
      batched {
        atScreen(Mfd.Osb.positions(2) - Vec2(0.0, 0.1)) {
          self.heading.round.toString.pad(3, '0').drawCentered(WHITE)
        }
      }
    }
  }

  def drawModes(game: GameData): Unit = {
    implicit val p = screenProjection
    val sensors = game.sensors.status
    val scale = config.symbolScale * 0.02 / font.getSpaceWidth

    batched {
      atScreen(-0.9, 0.65) {

        transform(_
          .scalexy(scale)) {

          var n = 0
          def drawTextLine(value: Any, color: Color): Unit = {
            transform(_.translate(y = -n.toFloat * font.getLineHeight)) {
              value.toString.drawRaw(xAlign = 0.5f, color = color)
            }
            n += 1
          }

          drawTextLine(s"${game.aircraftMode.master} / ${game.aircraftMode.submode}", LIGHT_GRAY)
          drawTextLine(s"${sensors.prf.selection} / ${sensors.prf.current}", LIGHT_GRAY)
        }
      }
    }
  }

}
