package se.gigurra.leavu3.mfd

import se.gigurra.leavu3.externaldata.{DlinkData, GameData}
import se.gigurra.leavu3.interfaces.MouseClick
import se.gigurra.serviceutils.twitter.logging.Logging

abstract class Page(val name: String) extends Logging {

  def mouseClicked(click: MouseClick): Unit =  {}

  def pressOsb(i: Int): Unit = {}

  def draw(game: GameData, dlinkIn: Map[String, DlinkData]): Unit

  val shortName = this.getClass.getSimpleName.toLowerCase.subSequence(0, 3)
  logger.info(s"Created $shortName mfd page")
}
