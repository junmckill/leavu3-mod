package se.gigurra.leavu3.mfd

import se.gigurra.leavu3.datamodel.{Bra, Configuration, Contact, DlinkData, GameData, Vec2, self}
import se.gigurra.leavu3.gfx.{BScopeProjection, Projection}
import se.gigurra.leavu3.interfaces.GameIn
import se.gigurra.leavu3.gfx.RenderContext._

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
          drawSelectedWaypoint(game)
          drawOwnContacts(game, dlinkIn)
          drawAiWingmen(game)
          drawAiWingmenTargets(game)
          drawDlinkWingmenAndTargets(game, dlinkIn)
          drawScanZoneOverlay(game)
          drawTdc(game)
        }
        drawSurroundEdge()
      }
    }
  }

  def drawScanZoneUnderlay[_: Projection](game: GameData): Unit = {

    def drawGreyUnderlay(): Unit = {
      val braMiddleScreen = Bra(self.heading, screenDistMeters*0.5, 0.0)
      at(self.position + braMiddleScreen.toOffset : Vec2, heading = self.heading) {
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


  def drawSelectedWaypoint[_: Projection](game: GameData): Unit = {
    drawWp(game.route.currentWaypoint, None, selected = true)
  }

  def drawAiWingmen[_: Projection](game: GameData): Unit = {

  }

  def drawAiWingmenTargets[_: Projection](game: GameData): Unit = {

  }

  def drawDlinkWingmenAndTargets[_: Projection](game: GameData, dlinkIn: Seq[(String, DlinkData)]): Unit = {

  }

  def drawScanZoneOverlay[_: Projection](game: GameData): Unit = {

  }

  def drawOwnContacts[_: Projection](game: GameData, dlinkIn: Seq[(String, DlinkData)]): Unit = {

    val contacts = new mutable.HashMap[Int, Contact] // id -> data
    val order = new mutable.HashMap[Int, Int] // id -> index
    val rws = new mutable.HashMap[Int, Boolean] // id -> index

    import game.sensors.targets._

    for {
      (collection, isRws) <- Seq((detected, true), (tws.map(_.contact), false), (locked.map(_.contact), false))
      (contact, i) <- collection.zipWithIndex
    } {
      order.put(contact.id, i)
      contacts.put(contact.id, contact)
      rws.put(contact.id, isRws)
    }

    implicit class RichContact(c: Contact) {
      def index: Int = order(c.id)
      def isRws: Boolean = rws(c.id)
      def news: Double = GameIn.rwsContactNews(c).getOrElse(1.0)
    }

    val positionsEchoed = contacts.values.toSeq
      .filterNot(_.isDesignated)
      .filter(_.isPositionKnown)
      .sortBy(_.index)

    val positionsDesignated = contacts.values.toSeq
      .filter(_.isDesignated)
      .filter(_.isPositionKnown)
      .sortBy(_.index)

    val bearingsDesignated = contacts.values.toSeq
      .filter(_.isDesignated)
      .filterNot(_.isPositionKnown)
      .sortBy(_.index)

    for (contact <- bearingsDesignated) {
   //   val offs = contact.position - self.position : Vec2
   //   lines(Seq(Vec2() -> offs) * 10000.0, YELLOW)
    }

    def drawKnownPosContacts(contacts: Seq[Contact]): Unit = {
      for (contact <- contacts.reverse) { // draw lowest index (=highest prio) last

        val baseColor = contactColor(contact, fromDatalink = false)
        val color = if (contact.isRws) baseColor.scaleAlpha(contact.news) else baseColor

        drawContact(
          position = contact.position,
          heading = if (contact.isRws) None else Some(contact.heading),
          color = color,
          centerText = if (contact.isDesignated) (contact.index + 1).toString else "",
          fill = contact.isDesignated,
          drawAlt = !contact.isRws
        )
      }
    }

    // Separated to ensure draw order
    drawKnownPosContacts(positionsEchoed)
    drawKnownPosContacts(positionsDesignated)
  }

  def drawSurroundEdge[_: Projection](): Unit = {
    rect(screenDistMeters, screenDistMeters, color = TEAL)
  }

  def drawInfo(game: GameData, dlinkIn: Seq[(String, DlinkData)]): Unit = {
    implicit val p = screenProjection
    drawBullsEyeNumbers(game)
    drawBraNumbers(game)
    drawOwnHeading(game)
    drawModes(game)
    drawDlzs(game)
    drawTargetInfo(game)
    drawOsbs(game)
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

  def drawTargetInfo[_: Projection](game: GameData): Unit = {
  }

}
