package se.gigurra.leavu3.lmath

import se.gigurra.leavu3.datamodel._
import se.gigurra.leavu3.gfx.RenderContext

trait RichContact {  _: RenderContext.type =>

  implicit class RichContact(c: Contact) {

    def bearing: Double = {
      (c.position - self.position).asBra.bearing
    }

    def elevation: Double = {
      (c.position - self.position).asBra.elevation
    }
  }

  implicit class RichTarget(c: Target) extends RichContact(c.contact)
}
