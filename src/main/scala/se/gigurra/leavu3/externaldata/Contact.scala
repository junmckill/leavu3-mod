package se.gigurra.leavu3.externaldata

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Schema, Parsed}

case class Contact(source: SourceData) extends Parsed[Contact.type] {
  val id                     = parse(schema.id)
  val course                 = parse(schema.course).toDegrees
  val flags                  = parse(schema.flags)
  val aspect                 = parse(schema.aspect).toDegrees
  val verticalViewingAngle   = parse(schema.verticalViewingAngle).toDegrees
  val horizontalViewingAngle = parse(schema.horizontalViewingAngle).toDegrees
  val updatesNumber          = parse(schema.updatesNumber)
  val startOfLock            = parse(schema.startOfLock)
  val rcs                    = parse(schema.rcs)
  val forces                 = parse(schema.forces)
  val country                = parse(schema.country)
  val burnthrough            = parse(schema.burnthrough)
  val jamming                = parse(schema.jamming) != 0
  val closure                = parse(schema.closure)
  val machNumber             = parse(schema.machNumber)
  val spatial                = parse(schema.spatial)
  val typ                    = parse(schema.typ)
  val velocity               = parse(schema.velocity)
  val distance               = parse(schema.distance)
}

object Contact extends Schema[Contact] {
  val id                     = required[Int]("ID")
  val course                 = required[Float]("course")
  val flags                  = required[Int]("flags")
  val aspect                 = required[Float]("delta_psi")
  val verticalViewingAngle   = required[Float]("fin")
  val horizontalViewingAngle = required[Float]("fim")
  val updatesNumber          = required[Int]("updates_number")
  val startOfLock            = required[Double]("start_of_lock")
  val rcs                    = required[Double]("reflection")
  val forces                 = required[Vec3]("forces")
  val country                = required[Int]("country")
  val burnthrough            = required[Boolean]("jammer_burned")
  val jamming                = required[Int]("isjamming")
  val closure                = required[Float]("convergence_velocity")
  val machNumber             = required[Float]("mach")
  val spatial                = required[Spatial]("position")
  val typ                    = required[UnitType]("type")
  val velocity               = required[Vec3]("velocity")
  val distance               = required[Float]("distance")
}
