package se.gigurra.leavu3.externaldata

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Schema, Parsed}

case class Version(source: SourceData = Map.empty) extends Parsed[Version.type] {
  val productName    = parse(schema.productName)
  val fileVersion    = parse(schema.fileVersion).mkString(".")
  val productVersion = parse(schema.productVersion).mkString(".")
}

object Version extends Schema[Version] {
  val productName    = required[String]("ProductName", default = "")
  val fileVersion    = required[Seq[Int]]("FileVersion", default = Seq.empty)
  val productVersion = required[Seq[Int]]("ProductVersion", default = Seq.empty)
}
