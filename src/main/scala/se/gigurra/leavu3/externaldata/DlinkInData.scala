package se.gigurra.leavu3.externaldata

import com.twitter.finagle.FailedFastException
import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{MapDataParser, Parsed, Schema}
import se.gigurra.leavu3.util.{CurTime, RestClient, SimpleTimer}
import se.gigurra.leavu3.DlinkSettings
import se.gigurra.serviceutils.json.JSON
import se.gigurra.serviceutils.twitter.logging.Logging
import se.gigurra.serviceutils.twitter.service.ServiceException

import scala.util.{Failure, Success, Try}
import scala.language.implicitConversions

case class Mark(source: SourceData = Map.empty) extends Parsed[Mark.type] {
  val id        = parse(schema.id)
  val position  = parse(schema.position)
  val timestamp = parse(schema.timestamp)
}

object Mark extends Schema[Mark] {
  val id        = required[String]("id", default = "")
  val position  = required[Vec3]("position", default = Vec3())
  val timestamp = required[Double]("timestamp", default = CurTime.seconds)

  def apply(id: String, position: Vec3): Mark = marshal(
    this.id -> id,
    this.position -> position
  )
}

case class Member(source: SourceData = Map.empty) extends Parsed[Member.type] {
  val planeId   = parse(schema.planeId)
  val modelTime = parse(schema.modelTime)
  val position  = parse(schema.position)
  val velocity  = parse(schema.velocity)
  val selfData  = parse(schema.selfData)
  val targets   = parse(schema.targets)
  val marks     = parse(schema.markPos)
  def pitch     = selfData.pitch
  def roll      = selfData.roll
  def heading   = selfData.heading
}

object Member extends Schema[Member] {
  val planeId   = required[Int]("playerPlaneId", default = 0)
  val modelTime = required[Double]("modelTime", default = 0)
  val position  = required[Vec3]("position", default = Vec3())
  val velocity  = required[Vec3]("velocity", default = Vec3())
  val selfData  = required[SelfData]("selfData", default = SelfData())
  val targets   = required[Seq[Target]]("targets", default = Seq.empty)
  val markPos   = required[Map[String, Mark]]("markPos", default = Map.empty[String, Mark])
}

case class DlinkData(source: SourceData = Map.empty) extends Parsed[DlinkData.type] {
  val timestamp = parse(schema.timestamp)
  val age       = parse(schema.age)
  val data      = parse(schema.data)
}

object DlinkData extends Schema[DlinkData] {
  val timestamp = required[Double]("timestamp", default = CurTime.seconds)
  val age = required[Double]("age", default = 0.0)
  val data = required[Member]("data", default = Member())

  implicit def toMember(d: DlinkData): Member = d.data
}

object DlinkInData extends Logging {
  def startPoller(config: DlinkSettings): Unit = {

    val client = RestClient(config.host, config.port)

    SimpleTimer.fromFps(config.inFps) {
      Try {
        val rawData = JSON.readMap(client.getBlocking(config.team, cacheMaxAgeMillis = Some(10000L)))
        ExternalData.dlinkIn = rawData.collect {
          case ValidDlinkData(id, dlinkData) => id -> dlinkData
        }
      } match {
        case Success(_) =>
        case Failure(e: ServiceException) =>
          logger.error(s"Data link host replied with an error: $e")
        case Failure(e: FailedFastException) => // Ignore ..
        case Failure(e) =>
          logger.error(e, s"Unexpected error when attempting to receive from dlink")
      }
    }
  }

  object ValidDlinkData {
    def unapply(callsignAndData: (String, Any)): Option[(String, DlinkData)] = {
      val callsign = callsignAndData._1
      val raw = callsignAndData._2
      Try(implicitly[MapDataParser[DlinkData]].parse(raw)) match {
        case Success(dlinkData) => Some(callsign, dlinkData)
        case Failure(e) =>
          logger.warning(s"Failed to read dlink data from $callsign")
          None
      }
    }
  }

}