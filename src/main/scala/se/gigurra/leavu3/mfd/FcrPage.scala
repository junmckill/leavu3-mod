package se.gigurra.leavu3.mfd

import se.gigurra.leavu3.datamodel.{Bra, Configuration, Contact, DlinkData, GameData, Vec2, self}
import se.gigurra.leavu3.gfx.{BScopeProjection, Projection}
import se.gigurra.leavu3.interfaces.GameIn
import se.gigurra.leavu3.gfx.RenderContext._
import se.gigurra.leavu3.util.CurTime

import scala.collection.mutable

/**
  * Created by kjolh on 3/12/2016.
  */
case class FcrPage(implicit config: Configuration) extends Page("FCR") {

  def screenDistMeters: Double = GameIn.snapshot.sensors.status.scale.distance
  def screenWidthDegrees: Double = GameIn.snapshot.sensors.status.scale.azimuth
  val inset = 0.2

  def draw(game: GameData, dlinkIn: Seq[(String, DlinkData)]): Unit = {
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
          drawSelectedWaypoint(game)
          drawDlinkMarks(dlinkIn)
          drawAiWingmen(game)
          drawAiWingmenTargets(game)
          drawDlinkMembersAndTargets(dlinkIn)
          drawOwnContacts(game, dlinkIn)
          drawTdc(game)
          drawScanZoneOverlay(game)
        }
        drawSurroundEdge()
      }
    }
  }

  def braMiddleOfScreen = Bra(self.heading, screenDistMeters*0.5, 0.0)

  def drawScanZoneUnderlay[_: Projection](game: GameData): Unit = {

    def drawGreyUnderlay(): Unit = {
      at(self.position + braMiddleOfScreen.toOffset : Vec2, heading = self.heading) {
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

    implicit class RichContact(c: Contact) {
      def index: Int = order(c.id)
      def news: Double = GameIn.rdrMemory(c).fold(1.0)(_.news)
    }

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
      for (contact <- contacts.reverse) { // draw lowest index (=highest prio) last

        val baseColor = contactColor(contact, fromDatalink = false)
        val color = baseColor.scaleAlpha(contact.news)
        val lag = if (contact.isDesignated || contact.isRws) 0.0 else GameIn.rdrLastTwsPositionUpdate(contact).fold(0.0)(CurTime.seconds - _.timestamp)
        val position = contact.position + contact.velocity * lag

        drawContact(
          position = position,
          heading = if (contact.isRws) None else Some(contact.heading),
          color = color,
          centerText = if (contact.isDesignated) (contact.index + 1).toString else "",
          fill = contact.isDesignated,
          drawAlt = !contact.isRws
        )
      }
    }

    def drawJammers(contacts: Seq[Contact]): Unit = {
      for (contact <- contacts.reverse) { // draw lowest index (=highest prio) last

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

        val braLeftEdge = Bra(azLeftEdge, screenDistMeters * symbolOffs, 0.0)
        val braRightEdge = Bra(azRightEdge, screenDistMeters * symbolOffs, 0.0)

        val leftPos = self.position + braLeftEdge.toOffset : Vec2
        val rightPos = self.position + braRightEdge.toOffset : Vec2
        val centerPos = 0.5 * (leftPos + rightPos)

        val h = 0.05

        for (pos <- Seq(leftPos, centerPos, rightPos)) {
          at(pos, heading = self.heading) {
            lines(Seq(Vec2(0.0, -h) -> Vec2(0.0, h)) * symbolScale)
          }
        }
      }
    }

    def drawVerticalStuff(): Unit = {
      if (game.sensors.status.sensorOn) {

        def elev2dist(elev: Double): Double = screenDistMeters/2.0 + screenDistMeters * elev / 180.0

        val symbolOffs = 0.025

        val (elDir, elWidth) = scanZoneElDirectionAndHeight
        val az = self.heading - (1.0 - symbolOffs) * game.sensors.status.scale.azimuth / 2.0
        val distCenter = elev2dist(elDir)
        val distUp = elev2dist(elDir + elWidth / 2.0)
        val distDown = elev2dist(elDir - elWidth / 2.0)

        val braCenter = Bra(az, distCenter, 0.0)
        val braUp = Bra(az, distUp, 0.0)
        val braDown = Bra(az, distDown, 0.0)

        val center = self.position + braCenter.toOffset : Vec2
        val up = self.position + braUp.toOffset : Vec2
        val down = self.position + braDown.toOffset : Vec2

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
    drawBullsEyeNumbers(game)
    drawBraNumbers(game)
    drawOwnHeading(game)
    drawModes(game)
    drawDlzs(game)
    drawTargetInfo(game)
    drawOffText(game)
    drawOsbs(game)
  }

  def drawBearings[_: Projection](game: GameData): Unit = {
  }

  def drawElevations[_: Projection](game: GameData): Unit = {
  }

  def drawBullsEyeNumbers[_: Projection](game: GameData): Unit = {
  }

  def drawBraNumbers[_: Projection](game: GameData): Unit = {
  }

  def drawOwnHeading[_: Projection](game: GameData): Unit = {
  }

  def drawModes[_: Projection](game: GameData): Unit = {
  }

  def drawOsbs[_: Projection](game: GameData): Unit = {
  }

  def drawDlzs[_: Projection](game: GameData): Unit = {
  }

  def drawOffText[_: Projection](game: GameData): Unit = {
  }

  def drawTargetInfo[_: Projection](game: GameData): Unit = {
  }

}
