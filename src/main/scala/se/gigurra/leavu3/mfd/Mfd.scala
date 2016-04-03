package se.gigurra.leavu3.mfd

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import se.gigurra.leavu3.app.Instrument
import se.gigurra.leavu3.datamodel.{Configuration, DlinkData, GameData, Vec2}
import se.gigurra.leavu3.gfx.ScreenProjection

import scala.language.postfixOps
import se.gigurra.leavu3.gfx.RenderContext._
import se.gigurra.leavu3.interfaces.{KeyPress, MouseClick}
import se.gigurra.leavu3.lmath.Box

case class Mfd(implicit config: Configuration)
  extends Instrument(config)
  with MfdIfc {

  implicit val _p = ScreenProjection()
  implicit val mfdIfc = this

  val hsd = HsdPage()
  val rwr = RwrPage()
  val sms = SmsPage()
  val fcr = FcrPage()
  val inf = InfoPage()
  val blank = BlankPage()
  val available = Seq(hsd, rwr, sms, fcr, inf, blank)
  var qPages = config.qps.zipWithIndex.map(p => (p._2, pageByName(p._1))).toMap
  var iQPage = config.initialQp
  var mainMenuOpen: Boolean = false
  val keyBindings = MfdKeyBindings(config.keyBindings)
  var isDcltOn = config.dclt
  var shouldDrawOsbs = config.osbs

  def pageByName(name: String): Page = {
    available.find(_.name == name.toUpperCase).getOrElse(throw new RuntimeException(s"No Mfd Page found for name: $name"))
  }

  def currentPage: Option[Page] = qPages.get(iQPage)

  def qp2Osb(qp: Int): Int = 14 - qp

  def osb2Qp(osb: Int): Int = 14 - osb

  def drawPage(game: GameData, dlinkIn: Seq[(String, DlinkData)]): Unit = {
    currentPage.foreach(_.draw(game, dlinkIn))
  }

  def drawOsbs(): Unit = {
    for ((iQp, page) <- qPages) {
      if (iQPage == iQp || verbose) {
        osb.drawHighlighted(qp2Osb(iQp), page.name, iQp == iQPage)
      }
    }
    osb.drawBoxed(15, "OSB", boxed = shouldDrawOsbs, forceDraw = true)
    osb.drawBoxed(9, "DCL", boxed = isDcltOn)
  }

  def drawMainMenu(): Unit = {
    batched {
      for (pos <- Mfd.Osb.positions) {
        at(pos) {
          "Item".drawCentered(Color.WHITE)
        }
      }
    }
  }

  def update(game: GameData, dlinkIn: Seq[(String, DlinkData)]): Unit = frame {
    mainMenuOpen = false // Add support for menu later when we have more than 3 pages implemented
    if (mainMenuOpen) {
      drawMainMenu()
    } else {
      drawPage(game, dlinkIn)
      drawOsbs()
    }
  }

  def changeQpByOffset(offet: Int): Unit = {
    if (!mainMenuOpen) {
      iQPage = math.max(0, (5 + iQPage + offet) % 5)
    }
  }

  def keyPressed(press: KeyPress): Unit = {
    press match {
      case keyBindings.NEXT_QP() => changeQpByOffset(1)
      case keyBindings.PREV_QP() => changeQpByOffset(-1)
      case keyBindings.OSB(i) => pressOsb(i)
      case _ =>
    }
  }

  def isQpOsb(i: Int): Boolean = {
    10 <= i && i <= 14
  }

  def pressOsb(i: Int): Unit = {
    if (mainMenuOpen) {
      pressOsbInMenu(i)
    } else {
      if (isQpOsb(i)) {
        pressQpOsb(osb2Qp(i))
      } else if (i == 9) {
        isDcltOn = !isDcltOn
      } else if (i == 15) {
        shouldDrawOsbs = !shouldDrawOsbs
      } else {
        currentPage.foreach(_.pressOsb(i))
      }
    }
  }

  def mouseClicked(click: MouseClick): Unit = {
    (0 until 20).find { i =>
      val center = Mfd.Osb.positions(i)
      val height = Mfd.Osb.boxHeight * config.symbolScale
      val width = Mfd.Osb.boxWidth * config.symbolScale
      val hitBox = Box(width, height, center)
      hitBox.contains(click.ortho11Raw)
    } match {
      case Some(i) => pressOsb(i)
      case None =>
        if (!mainMenuOpen) {
          currentPage.foreach(_.mouseClicked(click))
        }
    }
  }

  def priority: Int = {
    currentPage.fold(0)(_.priority)
  }

  def pressOsbInMenu(i: Int): Unit = {
    mainMenuOpen = false
  }

  def pressQpOsb(i: Int): Unit = {
    if (i == iQPage) {
      mainMenuOpen = true
    } else {
      if (qPages.contains(i))
        iQPage = i
    }
  }

  val osb = new OsbIfc {

    def draw(iOsb: Int,
             text: String,
             boxType: ShapeType = null,
             color: Color = null,
             forceDraw: Boolean = false)(implicit config: Configuration): Unit = {

      if (shouldDrawOsbs || forceDraw) {

        implicit val _p = ScreenProjection()

        val white = if (color != null) color else LIGHT_GRAY
        val black = BLACK

        if (boxType != null) {
          at(Mfd.Osb.positions(iOsb)) {

            val extraWidth = if (text.length > 3) {
              text.length.toFloat / 3.0f
            } else {
              1.0f
            }

            rect(symbolScale * Mfd.Osb.boxWidth * extraWidth, symbolScale * Mfd.Osb.boxHeight, color = white, typ = boxType)
          }
        }

        val pos = Mfd.Osb.positions(iOsb)
        at(pos) {
          text.drawCentered(if (boxType == FILL) black else white)
        }

      }

    }

  }

}

trait MfdIfc {

  def isDcltOn: Boolean
  def verbose: Boolean = !isDcltOn
  def shouldDrawOsbs: Boolean
  def osb: OsbIfc

  trait OsbIfc {

    def drawBoxed(iOsb: Int,
                  text: String,
                  boxed: Boolean = true,
                  color: Color = null,
                  forceDraw: Boolean = false)(implicit config: Configuration): Unit = {
      draw(iOsb, text, if (boxed) LINE else null, color, forceDraw = forceDraw)
    }

    def drawHighlighted(iOsb: Int,
                        text: String,
                        highlighted: Boolean = true,
                        color: Color = null,
                        forceDraw: Boolean = false)(implicit config: Configuration): Unit = {
      draw(iOsb, text, if (highlighted) FILL else null, color, forceDraw = forceDraw)
    }

    def draw(iOsb: Int,
             text: String,
             boxType: ShapeType = null,
             color: Color = null,
             forceDraw: Boolean = false)(implicit config: Configuration)
  }
}

object Mfd {

  object Osb {
    val nPerSide = 5
    val boxWidth = 0.10f
    val boxHeight = 0.05f
    val offs = 0.4f
    val inset = 0.1f
    val wholeWidth = 2.0f - offs * 2.0f
    val step = wholeWidth / (nPerSide - 1).toFloat

    val upperLeftBox = Vec2(-1.0f + offs, 1.0f - inset)
    val upperRightBox = Vec2(1.0f - inset, 1.0f - offs)
    val lowerRightBox = Vec2(1.0f - offs, -1.0f + inset)
    val lowerLeftBox = Vec2(-1.0f + inset, -1.0f + offs)

    val upperBoxCenters = Seq(
      upperLeftBox + 0.0 * Vec2(step, 0.0),
      upperLeftBox + 1.0 * Vec2(step, 0.0),
      upperLeftBox + 2.0 * Vec2(step, 0.0),
      upperLeftBox + 3.0 * Vec2(step, 0.0),
      upperLeftBox + 4.0 * Vec2(step, 0.0)
    )

    val rightBoxCenters = Seq(
      upperRightBox + 0.0 * Vec2(0.0, -step),
      upperRightBox + 1.0 * Vec2(0.0, -step),
      upperRightBox + 2.0 * Vec2(0.0, -step),
      upperRightBox + 3.0 * Vec2(0.0, -step),
      upperRightBox + 4.0 * Vec2(0.0, -step)
    )

    val lowerBoxCenters = Seq(
      lowerRightBox + 0.0 * Vec2(-step, 0.0),
      lowerRightBox + 1.0 * Vec2(-step, 0.0),
      lowerRightBox + 2.0 * Vec2(-step, 0.0),
      lowerRightBox + 3.0 * Vec2(-step, 0.0),
      lowerRightBox + 4.0 * Vec2(-step, 0.0)
    )

    val leftBoxCenters = Seq(
      lowerLeftBox + 0.0 * Vec2(0.0, step),
      lowerLeftBox + 1.0 * Vec2(0.0, step),
      lowerLeftBox + 2.0 * Vec2(0.0, step),
      lowerLeftBox + 3.0 * Vec2(0.0, step),
      lowerLeftBox + 4.0 * Vec2(0.0, step)
    )

    val positions = Seq(upperBoxCenters, rightBoxCenters, lowerBoxCenters, leftBoxCenters).flatten

  }
}