package se.gigurra.leavu3.mfd

import com.badlogic.gdx.graphics.Color
import se.gigurra.leavu3.datamodel._
import se.gigurra.leavu3.gfx.RenderContext._
import se.gigurra.leavu3.util.CircleBuffer
import se.gigurra.leavu3.interfaces.{Dlink, MouseClick}

import scala.collection.mutable
import scala.language.postfixOps

/**
  * Created by kjolh on 3/12/2016.
  */
case class HsdPage(implicit config: Configuration, mfd: MfdIfc) extends Page("HSD", Page.Priorities.HSD) {

  var shouldDrawDetailedHsi = config.hsdHsi
  var shouldDrawOwnHeading = config.hsdHeading
  var shouldDrawModes = config.hsdModes
  val deprFactor = CircleBuffer(0.0, 0.5).withDefaultValue(0.5)

  val OSB_DEPR = 1
  val OSB_HDG = 2
  val OSB_SCALE = 17
  val OSB_MODES = 16
  val OSB_HSI = 3
  val OSB_DEL = 7
  val OSB_UNITS = 8

  override def pressOsb(i: Int): Unit = {
    i match {
      case OSB_DEPR => deprFactor.stepDown()
      case OSB_HSI => shouldDrawDetailedHsi = !shouldDrawDetailedHsi
      case OSB_HDG => shouldDrawOwnHeading = !shouldDrawOwnHeading
      case OSB_DEL => deleteOwnMarkpoint()
      case OSB_MODES => shouldDrawModes = !shouldDrawModes
      case OSB_UNITS => stepDisplayUnits()
      case _ => // Nothing yet
    }
  }

  override def mouseClicked(click: MouseClick): Unit =  {
    val screenCenter = Vec2(0.0, -deprFactor)
    val offs = (click.ortho11 - screenCenter) * distScale
    val relativeBra = offs.asBra
    val bra = relativeBra.copy(bearingRaw = self.heading + relativeBra.bearing)
    val clickPos = self.position + bra.toOffset
    addOwnMarkpoint(clickPos)
  }

  override def draw(game: GameData, dlinkIn: Seq[(String, DlinkData)]): Unit = {
    matchIngameScale(game)
    viewport(viewportSize = distScale * 2.0, offs = Vec2(0.0, -distScale * deprFactor), heading = self.heading){
      implicit val p = worldProjection
      drawSelf(game)
      drawHsi(game)
      drawWayPoints(game)
      drawDlinkMarks(dlinkIn)
      drawScanZone(game)
      drawAiWingmen(game)
      drawAiWingmenTargets(game)
      drawDlinkMembersAndTargets(dlinkIn)
      drawLockedTargets(game)
      drawTdc(game)
    }(worldProjection)
    drawBullsEyeNumbrs(game)
    drawBraNumbrs(game)
    drawOwnHeading(game)
    if (shouldDrawModes && verbose)
      drawModes(game)
    drawOsbs(game, dlinkIn)
  }

  def drawHsi(game: GameData): Unit = {
    drawHsi(close = true, middle = true, far = true, tics = shouldDrawDetailedHsi)(worldProjection)
  }

  def drawSelf(game: GameData): Unit = {
    drawSelf(0.0)(worldProjection)
  }

  def drawWayPoints(game: GameData): Unit = {
    implicit val p = worldProjection

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
    implicit val p = worldProjection
    val sensors = game.sensors.status
    if (sensors.sensorOn) {
      val dist = sensors.scale.distance
      val (direction, width) = scanZoneAzDirectionAndWidth
      arc(radius = dist, angle = width, direction = direction, color = LIGHT_GRAY)
    }
  }

  def drawLockedTargets(game: GameData): Unit = {
    implicit val p = worldProjection

    val contacts = new mutable.HashMap[Int, Contact] // id -> data
    val order = new mutable.HashMap[Int, Int] // id -> index
    import game.sensors.targets._

    for {
      collection <- Seq(detected, tws.map(_.contact), locked.map(_.contact))
      (contact, i) <- collection.zipWithIndex
    } {
      order.put(contact.id, i)
      contacts.put(contact.id, contact)
    }

    val fromOwnRadar = Contact.FromOwnRadar(order)
    import fromOwnRadar._

    val positionsDesignated = contacts.values.toSeq
      .filter(_.isDesignated)
      .filter(_.isPositionKnown)
      .sortBy(_.index)

    val bearingsDesignated = contacts.values.toSeq
      .filter(_.isDesignated)
      .filterNot(_.isPositionKnown)
      .sortBy(_.index)

    for (contact <- bearingsDesignated) {
      lineBetween(self.position, contact.position, YELLOW, scaleOut = 10000.0)
    }

    for (contact <- positionsDesignated.reverse) { // draw lowest index (=highest prio) last
      drawOwnContact(contact, fromOwnRadar)
    }
  }

  def drawBullsEyeNumbrs(game: GameData) = {
    drawBullsEyeNumbers(game, 0.02, (-0.9, 0.9))
  }

  def drawBraNumbrs(game: GameData) = {
    drawBraNumbers(game, 0.02, (0.45, 0.9))
  }

  def ownMarkpointActive: Boolean = {
    Dlink.Out.hasMark("own")
  }

  def addOwnMarkpoint(clickPos: Vec3): Unit = {
    Dlink.Out.addMark("own", Mark(Dlink.config.callsign, clickPos))
  }

  def deleteOwnMarkpoint(): Unit = {
    Dlink.Out.deleteMark("own")
  }

  def drawOsbs(game: GameData, dlinkIn: Seq[(String, DlinkData)]): Unit = {
    implicit val p = screenProjection
    if (verbose) {
      osb.drawBoxed(OSB_DEPR, "DEP", boxed = deprFactor.index != 0)
      osb.drawBoxed(OSB_HDG, "HDG", boxed = shouldDrawOwnHeading)
      osb.drawBoxed(OSB_HSI, "HSI", boxed = shouldDrawDetailedHsi)
    }
    if (ownMarkpointActive)
      osb.drawBoxed(OSB_DEL, "DEL", boxed = false, forceDraw = true)
    if (verbose) {
      osb.drawBoxed(OSB_MODES, "MOD", boxed = shouldDrawModes)
      osb.draw(OSB_UNITS, displayUnitName.toUpperCase.take(3))
    }
    osb.drawBoxed(OSB_SCALE, (distScale.get * m_to_distUnit).round.toString, boxed = false, forceDraw = true)
  }

  def drawOwnHeading(game: GameData): Unit = {
    implicit val p = screenProjection
    if (shouldDrawOwnHeading) {
      batched {
        at(Mfd.Osb.positions(2) - Vec2(0.0, 0.1)) {
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
      at((-0.9, 0.60)) {

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
