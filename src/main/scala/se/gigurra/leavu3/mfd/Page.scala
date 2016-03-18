package se.gigurra.leavu3.mfd

import se.gigurra.leavu3.DlinkData
import se.gigurra.leavu3.externaldata.{KeyPress, GameData}
import se.gigurra.serviceutils.twitter.logging.Logging

abstract class Page extends Logging {

  def update(game: GameData, dlinkIn: Map[String, DlinkData]): Unit

  def keyPressed(press: KeyPress): Unit = {}

  val shortName = this.getClass.getSimpleName.toLowerCase.subSequence(0, 3)
  logger.info(s"Created $shortName mfd page")
}
