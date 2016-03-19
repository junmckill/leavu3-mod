package se.gigurra.leavu3.mfd

import se.gigurra.leavu3.externaldata.GameData

import com.badlogic.gdx.graphics.Color
import se.gigurra.leavu3.externaldata._
import se.gigurra.leavu3.gfx.PpiProjection
import se.gigurra.leavu3.gfx.RenderContext._
import se.gigurra.leavu3.{DlinkSettings, Configuration, DlinkData}

import scala.language.postfixOps

/**
  * Created by kjolh on 3/12/2016.
  */
case class RwrPage(implicit config: Configuration, dlinkSettings: DlinkSettings) extends Page("RWR") {
  implicit val projection = new PpiProjection
  val stdTextSize = 0.75f
  val distance = 100 nmi
  val minRangeOffset = distance * 0.05 * config.symbolScale

  override def pressOsb(i: Int): Unit = {
    i match {
      case _ => // Nothing yet
    }
  }

  override def draw(game: GameData, dlinkIn: Map[String, DlinkData]): Unit = {
    viewport(viewportSize = distance * 2.0 * 1.33333, offs = Vec2(0.0, 0.0), heading = self.heading) {
      drawSelf(game)
      drawHsi(game)
      drawNotchBlocks(game)
      drawPdtBearing(game.pdt)
      drawThreats(game)
    }
    drawMenuItems(game)
  }

  implicit class RichEmitter(e: Emitter) {
    def range: Double = {
      val scalable = distance - minRangeOffset
      minRangeOffset + scalable * (1.0 - math.pow(e.power, 2.0))
    }

    def color: Color = {
      e.signalType match {
        case "scan" => BROWN
        case _ => YELLOW
      }
    }
  }

  def drawPdtBearing(pdt: Option[Target]): Unit = pdt foreach { pdt =>
    val a = minRangeOffset * (pdt.position - self.position : Vec2).normalized
    val b = distance * (pdt.position - self.position : Vec2).normalized
    lines(Seq(a -> b), DARK_GRAY)
  }

  def drawThreats(game: GameData): Unit = {

    for (threat <- game.electronicWarf.rwr.emitters) {
      val bearing = threat.azimuth + self.heading
      val bra = Bra(bearing = bearing, range = threat.range, deltaAltitude = 0.0)

      val offset = bra.toOffset
      val edgeOffset = offset.normalized * distance

      at(self.position + offset, heading = bearing) {
        drawAirThreat(threat)
      }

      lines(Seq(offset -> edgeOffset), threat.color)

    }
  }

  def drawAirThreat(threat: Emitter): Unit = {
    val w = 0.015
    val h = 0.035
    lines(Seq(
      Vec2(0.0, 0.0) -> Vec2(w, h),
      Vec2(0.0, 0.0) -> Vec2(-w, h),
      Vec2(w, h) -> Vec2(-w, h)
    ) * symbolScale, threat.color)
  }

  def drawHsi(game: GameData): Unit = {
    circle(radius = distance * 1.00, color = DARK_GRAY)
    lines(
      shapes.hsi.flag * symbolScale + Vec2(0.0, distance * 1.00),
      shapes.hsi.eastPin * symbolScale + Vec2(distance * 1.00, 0.0),
      shapes.hsi.westPin * symbolScale + Vec2(-distance * 1.00, 0.0),
      shapes.hsi.southPin * symbolScale + Vec2(0.0, -distance * 1.00)
    )
  }

  def drawSelf(game: GameData): Unit = {
    transform(_.rotate(-self.heading)) {
      lines(shapes.self.coords * symbolScale, CYAN)
      circle(0.005 * symbolScale, color = CYAN, typ = FILL)
      circle(minRangeOffset, color = DARK_GRAY, typ = LINE)
    }
  }

  def drawWaypoints(game: GameData): Unit = {

    def wpByIndex(i: Int): Option[Waypoint] = {
      game.route.waypoints.find(_.index == i)
    }

    for (wp <- game.route.waypoints) {
      circle(at = wp.position - self.position, radius = 0.015 * symbolScale, color = WHITE)
      wpByIndex(wp.next) match {
        case None =>
        case Some(nextWp) =>
          val thisOne = wp.position - self.position
          val nextOne = nextWp.position - self.position
          lines(Seq(thisOne -> nextOne))
      }
    }
    circle(at = game.route.currentWaypoint.position - self.position, radius = 0.015 * symbolScale, typ = FILL, color = WHITE)

    batched {
      for (wp <- game.route.waypoints) {
        val text = if (wp.index > 0) (wp.index - 1).toString else "x"
        at(wp.position) {
          text.drawRightOf(scale = stdTextSize, color = WHITE)
        }
      }
    }
  }

  def drawNotchBlocks(game: GameData): Unit = {

    for (azimuth <- Seq(-90.0f, 90.0f)) {
      val bearing = azimuth + self.heading
      val bra = Bra(bearing = bearing, range = distance, deltaAltitude = 0.0)
      val offset = bra.toOffset

      val w = 0.02
      val h = 0.05

      at(self.position + offset, heading = bearing) {
        rect(w * symbolScale, h * symbolScale, color = DARK_GRAY, typ = FILL)
      }
    }
  }

  def drawMenuItems(game: GameData): Unit = {
  }
}
