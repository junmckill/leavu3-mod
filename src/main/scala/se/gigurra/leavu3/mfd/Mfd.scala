package se.gigurra.leavu3.mfd

import se.gigurra.leavu3.externaldata.{KeyPress, GameData}
import se.gigurra.leavu3.gfx.RenderContext._
import se.gigurra.leavu3.{DlinkSettings, Configuration, DlinkData, Instrument}

import scala.language.postfixOps

case class Mfd(implicit config: Configuration, dlinkSettings: DlinkSettings) extends Instrument(config, dlinkSettings) {

  val hsd = HsdPage()
  val rwr = RwrPage()
  val sms = SmsPage()
  val fcr = FcrPage()
  val available = Seq(hsd, rwr, sms, fcr)
  var qPages = Map[Int, Page](0 -> hsd, 1 -> rwr, 2-> sms)
  var iQPage = 0
  var mainMenuOpen: Boolean = false

  def currentPage: Option[Page] = qPages.get(iQPage)

  def updatePage(game: GameData, dlinkIn: Map[String, DlinkData]): Unit = {
    currentPage.foreach(_.update(game, dlinkIn))
  }

  def drawMainMenuIfOpen(): Unit = {
    if (mainMenuOpen) {
      // TODO: Draw something
    }
  }

  def update(game: GameData, dlinkIn: Map[String, DlinkData]): Unit = frame {
    updatePage(game, dlinkIn)
    drawMainMenuIfOpen()
  }

  def keyPressed(press: KeyPress): Unit = {
  }
}
