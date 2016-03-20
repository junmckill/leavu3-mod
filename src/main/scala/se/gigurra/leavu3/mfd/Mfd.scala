package se.gigurra.leavu3.mfd

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import se.gigurra.leavu3.app.Instrument
import se.gigurra.leavu3.datamodel.{Configuration, DlinkConfiguration, DlinkData, GameData, Vec2}
import se.gigurra.leavu3.gfx.ScreenProjection

import scala.language.postfixOps
import se.gigurra.leavu3.gfx.RenderContext._
import se.gigurra.leavu3.interfaces.{Key, KeyPress, MouseClick}
import se.gigurra.leavu3.lmath.Box

case class Mfd(implicit config: Configuration, dlinkSettings: DlinkConfiguration)
  extends Instrument(config, dlinkSettings) {

  implicit val _p = ScreenProjection()

  val hsd = HsdPage()
  val rwr = RwrPage()
  val sms = SmsPage()
  val fcr = FcrPage()
  val available = Seq(hsd, rwr, sms, fcr)
  var qPages = Map[Int, Page](0 -> hsd, 1 -> rwr/*, 2 -> fcr*/)
  var iQPage = 0
  var mainMenuOpen: Boolean = false

  def currentPage: Option[Page] = qPages.get(iQPage)

  def qp2Osb(qp: Int): Int = 13 - qp

  def osb2Qp(osb: Int): Int = 13 - osb

  def drawPage(game: GameData, dlinkIn: Map[String, DlinkData]): Unit = {
    currentPage.foreach(_.draw(game, dlinkIn))
  }

  def drawQps(): Unit = {
    for (iQp <- Seq(0, 1, 2)) {
      qPages.get(iQp).foreach { page =>
        Mfd.Osb.drawHighlighted(qp2Osb(iQp), page.name, iQp == iQPage)
      }
    }
  }

  def drawMainMenu(): Unit = {
    // TODO: Draw something
    for (pos <- Mfd.Osb.positions) {
      batched {
        atScreen(pos) {
          "Item".drawCentered(Color.WHITE)
        }
      }
    }
  }

  def update(game: GameData, dlinkIn: Map[String, DlinkData]): Unit = frame {
    mainMenuOpen = false // Add support for menu later when we have more than 3 pages implemented
    if (mainMenuOpen) {
      drawMainMenu()
    } else {
      drawPage(game, dlinkIn)
      drawQps()
    }
  }

  def keyPressed(press: KeyPress): Unit = {
    press match {
      case Key.OSB(i) => pressOsb(i)
      case _ =>
    }
  }

  def isQpOsb(i: Int): Boolean = {
    11 <= i && i <= 13
  }

  def pressOsb(i: Int): Unit = {
    if (mainMenuOpen) {
      pressOsbInMenu(i)
    } else {
      if (isQpOsb(i)) {
        pressQpOsb(osb2Qp(i))
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


    def drawBoxed(iOsb: Int,
                  text: String,
                  boxed: Boolean = true)(implicit config: Configuration): Unit = {
      draw(iOsb, text, if (boxed) LINE else null)
    }

    def drawHighlighted(iOsb: Int,
                        text: String,
                        highlighted: Boolean = true)(implicit config: Configuration): Unit = {
      draw(iOsb, text, if (highlighted) FILL else null)
    }

    def draw(iOsb: Int,
             text: String,
             boxType: ShapeType = null)(implicit config: Configuration): Unit = {

      implicit val _p = ScreenProjection()

      val white = LIGHT_GRAY
      val black = BLACK

      if (boxType != null) {
        atScreen(Mfd.Osb.positions(iOsb)) {
          rect(symbolScale * Mfd.Osb.boxWidth, symbolScale * Mfd.Osb.boxHeight, color = white, typ = boxType)
        }
      }

      batched {
        val pos = Mfd.Osb.positions(iOsb)
        atScreen(pos) {
          text.drawCentered(if (boxType == FILL) black else white)
        }
      }

    }


  }

}