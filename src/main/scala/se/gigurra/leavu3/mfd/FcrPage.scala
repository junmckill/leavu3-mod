package se.gigurra.leavu3.mfd

import se.gigurra.leavu3.datamodel.{Configuration, Contact, DlinkData, GameData, Vec2, Vec3, self}
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

    val contacts = new mutable.HashMap[Int, Contact] // id -> data
    val order = new mutable.HashMap[Int, Int] // id -> index
    val rws = new mutable.HashMap[Int, Boolean] // id -> index

    implicit class ContactWithIndex(c: Contact) {
      def index: Int = order(c.id)
      def isRws: Boolean = rws(c.id)
    }

    import game.sensors.targets._
    for {
      (collection, isRws) <- Seq((detected, true), (tws.map(_.contact), false), (locked.map(_.contact), false))
      (contact, i) <- collection.zipWithIndex
    } {
      order.put(contact.id, i)
      contacts.put(contact.id, contact)
      rws.put(contact.id, isRws)
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

    def drawKnownPosContacts(contacts: Seq[Contact], isDesignated: Boolean): Unit = {
      for (contact <- contacts) { // draw lowest index (=highest prio) last
        drawContact(
          position = contact.position,
          heading = if (contact.isRws) None else Some(contact.heading),
          color = contactColor(contact, fromDatalink = false),
          centerText = if (contact.isDesignated) (contact.index + 1).toString else "",
          fill = isDesignated
        )
      }
    }

    drawKnownPosContacts(positionsEchoed, isDesignated = false)
    drawKnownPosContacts(positionsDesignated, isDesignated = true)
  }

  def drawSurroundEdge[_: Projection](): Unit = {
    rect(screenDistMeters, screenDistMeters, color = TEAL)
  }

}
