package se.gigurra.leavu3.externaldata

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Schema, Parsed}

import scala.language.implicitConversions

case class Target(source: SourceData) extends Parsed[Target.type] {
  val contact = parse(schema.contact)
  val dlz     = parse(schema.dlz)
}

object Target extends Schema[Target] {
  val contact = required[Contact]("target")
  val dlz     = required[Dlz]("DLZ")
  implicit def target2Contact(target: Target): Contact = target.contact
}

