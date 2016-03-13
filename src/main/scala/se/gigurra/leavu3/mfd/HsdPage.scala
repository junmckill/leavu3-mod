package se.gigurra.leavu3.mfd

import java.time.Instant

import com.badlogic.gdx.graphics.Color
import se.gigurra.leavu3.externaldata._
import se.gigurra.leavu3.gfx.RenderContext._
import se.gigurra.leavu3.util.{CurTime, CircleBuffer}
import se.gigurra.leavu3.{Configuration, DlinkData}

import scala.collection.mutable
import scala.language.postfixOps

/**
  * Created by kjolh on 3/12/2016.
  */
case class HsdPage(implicit config: Configuration) extends Page {

  var shouldMatchIngameScale = true
  val distance = CircleBuffer(10 nmi, 20 nmi, 40 nmi, 80 nmi, 160 nmi).withDefaultValue(40 nmi)
  val deprFactor = CircleBuffer(0.0, 0.5).withDefaultValue(0.5)

  def update(game: GameData, dlinkIn: Map[String, DlinkData]): Unit = {
    matchIngameScale(game)
    ppi_viewport(viewportSize = distance * 2.0, offs = Vec2(0.0, -distance * deprFactor), heading = self.heading) {
      drawSelf(game)
      drawHsi(game)
      drawWaypoints(game)
      drawScanZone(game)
      drawAiWingmen(game)
      drawAiWingmenTargets(game)
      drawDlinkMembers(dlinkIn)
      drawLockedTargets(game)
      drawTdc(game)
    }
    drawBullsEyeNumbers(game)
    drawMenuItems(game)
  }

  def matchIngameScale(game: GameData) = {
    if (shouldMatchIngameScale) {
      val x = distance.items.minBy(x => math.abs(x - game.sensors.status.scale.distance))
      distance.set(x)
    }
  }

  def drawHsi(game: GameData): Unit = {
    circle(radius = distance     * 0.50, color = DARK_GRAY)
    circle(radius = distance     * 1.00)
    circle(radius = distance     * 1.50)
    lines(shapes.hsi.flag * symbolScale + Vec2(0.0, distance * 0.50),
      shapes.hsi.flag * symbolScale + Vec2(0.0, distance * 1.00),
      shapes.hsi.flag * symbolScale + Vec2(0.0, distance * 1.50),
      shapes.hsi.eastPin * symbolScale + Vec2(distance * 0.50, 0.0),
      shapes.hsi.eastPin * symbolScale + Vec2(distance * 1.00, 0.0),
      shapes.hsi.eastPin * symbolScale + Vec2(distance * 1.50, 0.0),
      shapes.hsi.westPin * symbolScale + Vec2(-distance * 0.50, 0.0),
      shapes.hsi.westPin * symbolScale + Vec2(-distance * 1.00, 0.0),
      shapes.hsi.westPin * symbolScale + Vec2(-distance * 1.50, 0.0),
      shapes.hsi.southPin * symbolScale + Vec2(0.0, -distance * 0.50),
      shapes.hsi.southPin * symbolScale + Vec2(0.0, -distance * 1.00),
      shapes.hsi.southPin * symbolScale + Vec2(0.0, -distance * 1.50)
    )
  }

  def drawSelf(game: GameData): Unit = {
    transform(_.rotate(-self.heading)) {
      lines(shapes.self.coords * symbolScale, CYAN)
      circle(0.005 * symbolScale, color = CYAN, typ = FILL)
    }
  }

  def drawWaypoints(game: GameData): Unit = {

    def wpByIndex(i: Int): Option[Waypoint] = {
      game.route.waypoints.find(_.index == i)
    }

    for (wp <- game.route.waypoints) {
      circle(at = wp.position - self.position, radius = 0.015 * symbolScale, color = WHITE)
      wpByIndex(wp.next) match {
        case None =>
        case Some(nextWp) =>
          val thisOne = wp.position - self.position
          val nextOne = nextWp.position - self.position
          lines(Seq(thisOne -> nextOne))
      }
    }
    circle(at = game.route.currentWaypoint.position - self.position, radius = 0.015 * symbolScale, typ = FILL, color = WHITE)

    batched {
      for (wp <- game.route.waypoints) {
        val text = if (wp.index > 0) (wp.index - 1).toString else "x"
        at(wp.position) {
          text.drawPpiRightOf(scale = 0.75f, color = WHITE)
        }
      }
    }
  }

  def drawScanZone(game: GameData): Unit = {
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
          val altText = (wingman.position.z * m_to_kft).round.toString
          altText.drawPpiLeftOf(color = CYAN)
          val nameText = "AI"
          nameText.drawPpiRightOf(scale = 0.5f, color = CYAN)
        }
      }
    }
  }

  var wingmenTgtsLastTminus1 = Seq.empty[Vec3]
  var wingmenTgtsLastTminus2 = Seq.empty[Vec3]

  def drawAiWingmenTargets(game: GameData): Unit = {
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
          val text = (tgtPos.z * m_to_kft).round.toString
          text.drawPpiLeftOf(color = RED)
        }
      }
    }

    if (wingmenTgtsLastTminus1 != game.aiWingmenTgts) {
      wingmenTgtsLastTminus2 = wingmenTgtsLastTminus1
      wingmenTgtsLastTminus1 = game.aiWingmenTgts
    }
  }

  def drawDlinkMembers(dlinkIn: Map[String, DlinkData]): Unit = {

    val dlinksOfInterest = dlinkIn.filter(m => m._2.data.planeId != self.planeId || m._1 != self.dlinkCallsign)
    val targetsToDraw = new mutable.HashMap[Int, (DlinkData, Target)]

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
          val altText = (memberPosition.z * m_to_kft).round.toString
          altText.drawPpiLeftOf(color = CYAN)
          val nameText = name.take(2)
          nameText.drawPpiRightOf(scale = 0.5f, color = CYAN)
        }
      }

      for (target <- member.targets) {

        val targetPosition = target.position + target.velocity * lag

        if (target.isPositionKnown) at(targetPosition, heading = target.heading) {
          circle(radius = radius, typ = FILL, color = contactColor(target, fromDatalink = true))
          lines(Seq(Vec2(0.0, radius) -> Vec2(0.0, radius * 3)))
          batched {
            val altText = (targetPosition.z * m_to_kft).round.toString
            altText.drawPpiLeftOf(color = contactColor(target, fromDatalink = true))
            val nameText = name.take(2)
            nameText.drawPpiCentered(scale = 0.35f, color = BLACK)
          }
        } else at(memberPosition) {
          val b = targetPosition - member.position: Vec2
          lines(Seq(Vec2() -> b) * 10000.0, contactColor(target, fromDatalink = true))
        }
      }

    }

  }

  def contactColor(contact: Contact, fromDatalink: Boolean): Color = {
    if (self.coalition == contact.country) {
      GREEN
    } else if (fromDatalink) {
      RED
    } else {
      YELLOW
    }
  }

  def drawLockedTargets(game: GameData): Unit = {

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
          val text = (contact.position.z * m_to_kft).round.toString
          text.drawPpiLeftOf(color = contactColor(contact, fromDatalink = false))
          (contact.index + 1).toString.drawPpiCentered(scale = 0.75f, color = BLACK)
        }
      }
    }

  }

  def drawTdc(game: GameData): Unit = {
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
        val coverageText =
          s"""${(coverage.max * m_to_kft).round}
             |${(coverage.min * m_to_kft).round}""".stripMargin
        batched {
          coverageText.drawPpiRightOf(scale = 0.5f, color = WHITE)
        }
      }
    }
  }

  def drawMenuItems(game: GameData): Unit = {
  }

  def drawBullsEyeNumbers(game: GameData) = {

    def mkBraString(prefix: String, bra: Bra): String = s"$prefix : ${bra.brString}"

    val bullsEye = game.route.currentWaypoint
    val selfBra = (self.position - bullsEye.position).asBra
    val scale = config.symbolScale * 0.02 / font.getSpaceWidth

    batched {

      transform(_
        .translate(-0.9f, 0.9f)
        .scalexy(scale)) {

        val beStr = s"bullseye : wp ${bullsEye.index - 1}"
        val selfStr = mkBraString("    self", selfBra)

        var n = 0
        def drawTextLine(str: String, color: Color): Unit = {
          transform(_.translate(y = -n.toFloat * font.getLineHeight))(str.drawRaw(xAlign = 0.5f, color = color))
          n += 1
        }

        drawTextLine(beStr, LIGHT_GRAY)
        drawTextLine(selfStr, CYAN)

        game.tdcPosition foreach { tdc =>
          val tdcBra = (tdc - bullsEye.position).asBra
          val tdcStr = mkBraString("     tdc", tdcBra)
          drawTextLine(tdcStr, WHITE)
        }

        game.pdt foreach { pdt =>
          val pdtBra = (pdt.position - bullsEye.position).asBra
          val pdtStr = mkBraString("     pdt", pdtBra)
          drawTextLine(pdtStr, contactColor(pdt, fromDatalink = false))
        }

      }

    }

  }

}
