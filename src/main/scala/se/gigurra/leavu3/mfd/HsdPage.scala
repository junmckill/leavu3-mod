package se.gigurra.leavu3.mfd

import se.gigurra.leavu3.Configuration
import se.gigurra.leavu3.externaldata._
import se.gigurra.leavu3.gfx.RenderContext._
import se.gigurra.leavu3.util.CircleBuffer

import scala.collection.mutable
import scala.language.postfixOps

/**
  * Created by kjolh on 3/12/2016.
  */
case class HsdPage(implicit config: Configuration) extends Page {

  var shouldMatchIngameScale = true
  val distance = CircleBuffer(10 nmi, 20 nmi, 40 nmi, 80 nmi, 160 nmi).withDefaultValue(40 nmi)
  val deprFactor = CircleBuffer(0.0, 0.5).withDefaultValue(0.5)

  def update(game: GameData, dlinkIn: DlinkInData, dlinkOut: DlinkOutData): Unit = {
    matchIngameScale(game)
    ppi_viewport(viewportSize = distance * 2.0, offs = Vec2(0.0, -distance * deprFactor), heading = self.heading) {
      drawSelf(game)
      drawHsi(game)
      drawWaypoints(game)
      drawScanZone(game)
      drawAiWingmen(game)
      drawAiWingmenTargets(game)
      drawDlinkWingmen(dlinkIn)
      drawDlinkWingmenTargets(dlinkIn)
      drawLockedTargets(game)
      drawTdc(game)
    }
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
      lines(shapes.self.coords * symbolScale, LIGHT_GRAY)
      circle(0.005 * symbolScale, color = LIGHT_GRAY, typ = FILL)
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
          text.drawRightOf(WHITE)
        }
      }
    }
  }

  def drawScanZone(game: GameData): Unit = {
    val sensors = game.sensors.status
    if (sensors.sensorOn) {
      val dist = sensors.scale.distance
      val width = sensors.scanZone.size.azimuth
      val direction = sensors.scanZone.direction.azimuth
      arc(radius = dist, angle = width, direction = self.heading + direction, color = LIGHT_GRAY)
    }
  }

  def drawAiWingmen(game: GameData): Unit = {
    for (wingman <- game.aiWingmen) {
      val p = (wingman.position - self.position).vec2
      val radius = 0.020 * symbolScale
      at(wingman.position, wingman.heading) {
        circle(radius = radius, color = CYAN)
        lines(Seq(Vec2(0.0, radius) -> Vec2(0.0, radius * 3)))
      }
    }

    batched {
      for (wingman <- game.aiWingmen) {
        at(wingman.position) {
          val text = (wingman.position.z * m_to_kft).round.toString
          text.drawLeftOf(color = CYAN)
        }
      }
    }
  }

  var wingmenTgtsLastTminus1 = Seq.empty[Vec3]
  var wingmenTgtsLastTminus2 = Seq.empty[Vec3]

  def drawAiWingmenTargets(game: GameData): Unit = {
    val radius = 0.020 * symbolScale

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
          text.drawLeftOf(color = RED)
        }
      }
    }

    if (wingmenTgtsLastTminus1 != game.aiWingmenTgts) {
      wingmenTgtsLastTminus2 = wingmenTgtsLastTminus1
      wingmenTgtsLastTminus1 = game.aiWingmenTgts
    }
  }

  def drawDlinkWingmen(dlinkIn: DlinkInData): Unit = {
  }

  def drawDlinkWingmenTargets(dlinkIn: DlinkInData): Unit = {
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

      at(contact.position) {
        circle(radius = radius, typ = FILL, color = YELLOW)
        rotatedTo(contact.heading) {
          lines(Seq(Vec2(0.0, radius) -> Vec2(0.0, radius * 3)))
        }
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
          text.drawLeftOf(color = YELLOW)
          (contact.index + 1).toString.drawCentered(scale = 0.75f, color = BLACK)
        }
      }
    }

  }

  def drawTdc(game: GameData): Unit = {

  }

  def drawMenuItems(game: GameData): Unit = {

    /*
    batched {
      val text =
        s"""
           |Heading: ${self.heading}
           |Velocity: ${self.velocity}
           |nwps: ${game.route.waypoints.size}""".stripMargin

      transform(_.scalexy(1.5f / text.width)) {
        text.draw(color = RED)
      }
    }*/
  }
}
