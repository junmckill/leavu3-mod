package se.gigurra.leavu3.mfd

import se.gigurra.leavu3.{Configuration, Instrument}
import se.gigurra.leavu3.externaldata.{DlinkInData, DlinkOutData, GameData}
import se.gigurra.leavu3.gfx.RenderContext._
import scala.language.postfixOps

case class Mfd(config: Configuration) extends Instrument(config) {

  val hsd = HsdPage(config)
  val rwr = RwrPage()
  val sms = SmsPage()
  val fcr = FcrPage()
  val available = Seq(hsd, rwr, sms, fcr)
  var qPages = Map[Int, Page](0 -> hsd, 1 -> rwr, 2-> sms)
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
