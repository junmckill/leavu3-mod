package se.gigurra.leavu3.mfd

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import se.gigurra.leavu3.externaldata.{GameData, _}
import se.gigurra.leavu3.gfx.RenderContext._
import se.gigurra.leavu3.gfx.{Blink, PpiProjection}
import se.gigurra.leavu3.{Configuration, DlinkData, DlinkSettings}

import scala.language.postfixOps

/**
  * Created by kjolh on 3/12/2016.
  */
case class RwrPage(implicit config: Configuration, dlinkSettings: DlinkSettings) extends Page("RWR") {
  implicit val projection = new PpiProjection
  val stdTextSize = 0.75f
  val distance = 100 nmi
  val minRangeOffset = distance * 0.05 * config.symbolScale
  val blinkSpeed = 1.0 / 3.0

  object airThreat {
    val w = 0.015
    val h = 0.035
    def draw(threat: Emitter): Unit = {
      val a = Vec2(0,0) * symbolScale
      val b = Vec2(w,h) * symbolScale
      val c = Vec2(-w,h) * symbolScale
      triangle(a, b, c, typ = threat.fillType, color = threat.color)
    }
  }

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
        case Emitter.RADAR_SEARCH   => BROWN
        case Emitter.RADAR_LOCK     => YELLOW
        case Emitter.MISSILE_LAUNCH => Blink(Seq(YELLOW, RED), blinkSpeed)
        case Emitter.MISSILE_ACTIVE => Blink(Seq(YELLOW, RED), blinkSpeed)
        case s =>
          logger.warning(s"RWR: Unknown signal type: $s")
          Color.YELLOW
      }
    }

    def fillType: ShapeRenderer.ShapeType = {
      e.signalType match {
        case Emitter.MISSILE_ACTIVE => FILL
        case _ => LINE
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

      at(self.position + offset, heading = bearing) {
        airThreat.draw(threat)
      }

      val lineStart = offset.normalized * (bra.range + airThreat.h * symbolScale)
      val edgeOffset = offset.normalized * distance
      lines(Seq(lineStart -> edgeOffset), threat.color)

    }
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
