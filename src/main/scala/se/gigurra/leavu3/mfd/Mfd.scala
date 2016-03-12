package se.gigurra.leavu3.mfd

import se.gigurra.leavu3.Instrument
import se.gigurra.leavu3.externaldata.{DlinkInData, DlinkOutData, GameData}
import se.gigurra.leavu3.gfx.RenderContext._
import scala.language.postfixOps

case class Mfd() extends Instrument {
  var qPages = Map[Int, Page](0 -> Pages.hsd, 1 -> Pages.rwr, 2-> Pages.sms)
  var iQPage = 0
  var mainMenuOpen: Boolean = false

  def currentPage: Option[Page] = qPages.get(iQPage)

  def updatePage(game: GameData, dlinkIn: DlinkInData, dlinkOut: DlinkOutData): Unit = {
    currentPage.foreach(_.update(game, dlinkIn, dlinkOut))
  }

  def drawMainMenuIfOpen(): Unit = {
    if (mainMenuOpen) {
      // TODO: Draw something
    }
  }

  def update(game: GameData, dlinkIn: DlinkInData, dlinkOut: DlinkOutData): Unit = frame {
    updatePage(game, dlinkIn, dlinkOut)
    drawMainMenuIfOpen()
  }

}
