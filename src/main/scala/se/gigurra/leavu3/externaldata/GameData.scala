package se.gigurra.leavu3.externaldata

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Schema, Parsed}
import se.gigurra.leavu3.util.{RestClient, SimpleTimer}
import se.gigurra.serviceutils.json.JSON

case class UnitType(source: SourceData) extends Parsed[UnitType.type] {
  val level1          = parse(schema.level1)
  val level2          = parse(schema.level2)
  val level3          = parse(schema.level3)
  val level4          = parse(schema.level4)
}

object UnitType extends Schema[UnitType] {
  val level1          = required[Int]("level1")
  val level2          = required[Int]("level2")
  val level3          = required[Int]("level3")
  val level4          = required[Int]("level4")
}

case class Emitter(source: SourceData) extends Parsed[Emitter.type] {
  val id          = parse(schema.id)
  val signalType  = parse(schema.signalType)
  val azimuth     = parse(schema.azimuth)
  val priority    = parse(schema.priority)
  val power       = parse(schema.power)
  val unitType    = parse(schema.unitType)
}

object Emitter extends Schema[Emitter] {
  val id          = required[Int]("ID")
  val signalType  = required[String]("SignalType")
  val azimuth     = required[Double]("Azimuth")
  val priority    = required[Double]("Priority")
  val power       = required[Double]("Power")
  val unitType    = required[UnitType]("Type")
}

case class Rwr(source: SourceData) extends Parsed[Rwr.type] {
  val emitters = parse(schema.emitters)
  val mode     = parse(schema.mode)
}

object Rwr extends Schema[Rwr] {
  val emitters = required[Seq[Emitter]]("Emitters")
  val mode     = required[Int]("Mode")
}

case class GameData(source: SourceData) extends Parsed[GameData.type] {
  // DCS Remote Metadata
  val err = parse(schema.err)
  // Actual game data
  val rwr = parse(schema.rwr)
}

case class CounterMeasures(source: SourceData) extends Parsed[CounterMeasures.type] {
  val chaff = parse(schema.chaff)
  val flare = parse(schema.flare)
}

object CounterMeasures extends Schema[CounterMeasures] {
  val chaff = required[Int]("chaff")
  val flare = required[Int]("flare")
}

case class PayloadStation(source: SourceData) extends Parsed[PayloadStation.type] {
}

object PayloadStation extends Schema[PayloadStation] {
}

case class Payload(source: SourceData) extends Parsed[Payload.type] {
  val stations = parse(schema.stations)
}

object Payload extends Schema[Payload] {
  val stations = required[Seq[PayloadStation]]("stations")
}

object GameData extends Schema[GameData] {

  // DCS Remote Metadata
  val err = optional[String]("err")

  // Actual game data
  val rwr             = optional[Rwr]("rwr")
  val counterMeasures = optional[CounterMeasures]("counterMeasures")
  val payload         = optional[Payload]("payload")


  ///////////////////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////


  val path = "export/dcs_remote_export_data()"

  def startPoller(fps: Int, addr: String, port: Int): Unit = {

    val client = RestClient(addr, port)

    SimpleTimer.fromFps(fps) {
      ExternalData.gameData = JSON.read(client.getBlocking(path, cacheMaxAgeMillis = Some((1000.0 / fps / 2.0).toLong)))
      println(JSON.write(ExternalData.gameData))
    }
  }

}


