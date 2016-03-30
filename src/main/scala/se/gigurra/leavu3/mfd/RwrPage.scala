package se.gigurra.leavu3.mfd

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import se.gigurra.leavu3.datamodel.{GameData, _}
import se.gigurra.leavu3.gfx.RenderContext._
import se.gigurra.leavu3.gfx.{Blink, PpiProjection, Projection, ScreenProjection}
import se.gigurra.leavu3.datamodel.Configuration
import se.gigurra.leavu3.util.CircleBuffer

import scala.language.postfixOps

/**
  * Created by kjolh on 3/12/2016.
  */
case class RwrPage(implicit config: Configuration) extends Page("RWR") {

  val blinkSpeed = 1.0 / 3.0
  var a2aFilter = CircleBuffer[LockLevel](LockLevel.Search, LockLevel.Lock, LockLevel.Launch)
  var a2gFilter = CircleBuffer[LockLevel](LockLevel.Search, LockLevel.Lock, LockLevel.Launch)

  var shouldDrawDetailedHsi = true
  val screenEdgeOffset = 0.75f
  val OSB_A2A = 1
  val OSB_A2G = 2
  val OSB_HSI = 3

  def minRangeOffset = distScale * 0.05 * config.symbolScale

  object airThreat {
    val w = 0.015
    val h = 0.035
    def draw[_: Projection](threat: Emitter): Unit = {
      val a = Vec2(0,0) * symbolScale
      val b = Vec2(w,h) * symbolScale
      val c = Vec2(-w,h) * symbolScale
      triangle(a, b, c, typ = threat.fillType, color = threat.color)
    }
  }

  object groundThreat {
    val w = airThreat.h // Must be same as airThreat.h to ensure line connects properly!
    def draw[_: Projection](threat: Emitter): Unit = {
      rect(w * symbolScale, w * symbolScale, at = Vec2(0.0, w/2 *symbolScale), typ = threat.fillType, color = threat.color)
    }
  }

  override def pressOsb(i: Int): Unit = {
    i match {
      //TODO: Impl Roll stab case OSB_HOR => shouldHorizonStabilize = !shouldHorizonStabilize
      case OSB_A2A => a2aFilter.stepUp()
      case OSB_A2G => a2gFilter.stepUp()
      case OSB_HSI => shouldDrawDetailedHsi = !shouldDrawDetailedHsi
      case _ => // Nothing yet
    }
  }

  override def draw(game: GameData, dlinkIn: Seq[(String, DlinkData)]): Unit = {
    viewport(viewportSize = distScale * 2.0 / screenEdgeOffset, offs = Vec2(0.0, 0.0), heading = self.heading) {
      drawSelf(game)
      drawHsi(game)
      drawNotchBlocks(game)
      drawTargetBearings(game)
      drawThreats(game)
    }(ppiProjection)
    drawInfoText(game)
    drawOsbs(game)
  }

  implicit class RichEmitter(e: Emitter) {

    def range: Double = {
      val scalable = distScale - minRangeOffset
      minRangeOffset + scalable * (1.0 - math.pow(e.power, e.typ.power2RangeExponent)) * e.typ.maxRangeIndication
    }

    def color: Color = {
      e.signalType match {
        case Emitter.RADAR_SEARCH   => BROWN
        case Emitter.RADAR_TWS      => BROWN
        case Emitter.RADAR_LOCK     => YELLOW
        case Emitter.MISSILE_LAUNCH => Blink(Seq(YELLOW, RED), blinkSpeed)
        case Emitter.MISSILE_ACTIVE => Blink(Seq(YELLOW, RED), blinkSpeed)
        case s =>
          logger.warning(s"RWR: Unknown signal type: $s")
          Color.YELLOW
      }
    }

    def isActiveMissile: Boolean = {
      e.signalType == Emitter.MISSILE_ACTIVE
    }

    def fillType: ShapeRenderer.ShapeType = {
      e.signalType match {
        case Emitter.MISSILE_ACTIVE => FILL
        case _ => LINE
      }
    }

    def rwrName: String = {
      if (e.signalType == Emitter.MISSILE_ACTIVE) {
        "M"
      } else {
        e.typ.shortName
      }
    }
  }

  def drawTargetBearings(game: GameData): Unit = for (target <- game.sensors.targets.locked) {
    implicit val _p = ppiProjection
    val a = minRangeOffset * (target.position - self.position : Vec2).normalized
    val b = (distScale:Double) * (target.position - self.position : Vec2).normalized
    lines(Seq(a -> b), DARK_GRAY)
  }

  def threatFilter(e: Emitter): Boolean = {
    if (e.typ.isFlyer) {
      e.level >= a2aFilter
    } else {
      e.level >= a2gFilter
    }
  }

  def drawThreats(game: GameData): Unit = {
    implicit val _p = ppiProjection

    val allEmitters = game.electronicWarf.rwr.emitters

    for (threat <- allEmitters.filter(threatFilter).sortBy(_.priority)) {
      val bearing = threat.azimuth + self.heading
      val bra = Bra(bearingRaw = bearing, range = threat.range, deltaAltitude = 0.0)

      val offset = bra.toOffset

      at(self.position + offset, heading = bearing) {
        if (threat.typ.isFlyer) {
          airThreat.draw(threat)
        } else {
          groundThreat.draw(threat)
        }
      }

      val lineStart = offset.normalized * (bra.range + airThreat.h * symbolScale)
      val edgeOffset = offset.normalized * distScale
      lines(Seq(lineStart -> edgeOffset), threat.color)

      if (threat.range / distScale > 0.5) {
        at(self.position + edgeOffset * 1.05f, heading = bearing - self.heading) {
          threat.rwrName.drawCentered(threat.color, scale = 0.7f)
        }
      } else {
        at(self.position + lineStart, heading = bearing - self.heading) {
          threat.rwrName.drawRightOf(threat.color, scale = 0.7f)
        }
      }

    }
  }

  def drawHsi(game: GameData): Unit = {
    drawHsi(close = false, middle = true, far = false, tics = shouldDrawDetailedHsi)(ppiProjection)
  }

  def drawSelf(game: GameData): Unit = {
    drawSelf(minRangeOffset)(ppiProjection)
  }

  def drawNotchBlocks(game: GameData): Unit = {
    implicit val _p = ppiProjection

    for (azimuth <- Seq(-90.0f, 90.0f)) {
      val bearing = azimuth + self.heading
      val bra = Bra(bearingRaw = bearing, range = distScale, deltaAltitude = 0.0)
      val offset = bra.toOffset

      val w = 0.02
      val h = 0.05

      at(self.position + offset, heading = bearing) {
        rect(w * symbolScale, h * symbolScale, color = DARK_GRAY, typ = FILL)
      }
    }
  }

  def drawInfoText(game: GameData): Unit = {
    implicit val _p = screenProjection

    val scale = config.symbolScale * 0.02 / font.getSpaceWidth

    batched { at(-0.9, 0.9) {

      transform(_
        .scalexy(scale)) {

        var n = 0
        def drawTextLine(str: String, color: Color): Unit = {
          transform(_.translate(y = -n.toFloat * font.getLineHeight))(str.drawRaw(xAlign = 0.5f, color = color))
          n += 1
        }

        def getFilterColor(level: LockLevel): Color = {
          level match {
            case LockLevel.Search => LIGHT_GRAY
            case LockLevel.Lock => TAN
            case _ => BROWN
          }
        }

        drawTextLine("Filters", LIGHT_GRAY)
        drawTextLine(" a2a : " + a2aFilter.get, getFilterColor(a2aFilter))
        drawTextLine(" a2g : " + a2gFilter.get, getFilterColor(a2gFilter))

      }
    }}
  }

  def drawOsbs(game: GameData): Unit = {
    implicit val _p = screenProjection
    import Mfd.Osb._

    def drawFilterOsb(i: Int, str: String, setting: LockLevel): Unit = {
      setting match {
        case LockLevel.Search => drawHighlighted(i, str)
        case LockLevel.Lock => drawBoxed(i, str)
        case _ => Mfd.Osb.draw(i, str)
      }
    }

    drawFilterOsb(OSB_A2A, "A2A", a2aFilter)
    drawFilterOsb(OSB_A2G, "A2G", a2gFilter)
    drawBoxed(OSB_HSI, "HSI", shouldDrawDetailedHsi)
  }
}
