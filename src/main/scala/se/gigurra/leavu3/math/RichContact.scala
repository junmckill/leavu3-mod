package se.gigurra.leavu3.math

import se.gigurra.leavu3.externaldata.{Target, Contact}
import se.gigurra.leavu3.gfx.RenderContext

trait RichContact {  _: RenderContext.type =>
  implicit class RichContact(c: Contact) {
    def bearing: Double = {
      (c.position - self.position).asBra.bearing
    }
  }

  implicit class RichTarget(c: Target) extends RichContact(c.contact)
}
