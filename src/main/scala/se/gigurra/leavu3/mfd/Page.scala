package se.gigurra.leavu3.mfd

import se.gigurra.leavu3.datamodel.{Configuration, DlinkData, GameData}
import se.gigurra.leavu3.gfx.{PpiProjection, ScreenProjection}
import se.gigurra.leavu3.interfaces.MouseClick
import se.gigurra.serviceutils.twitter.logging.Logging

abstract class Page(val name: String, config: Configuration) extends Logging {

  val ppiProjection = new PpiProjection
  val screenProjection = new ScreenProjection
  val displayUnits = DisplayUnits.displayUnits.setBy(_.name == config.initialUnits)
  val shortName = this.getClass.getSimpleName.toLowerCase.subSequence(0, 3)
  logger.info(s"Created $shortName mfd page")

  def mouseClicked(click: MouseClick): Unit =  {}

  def pressOsb(i: Int): Unit = {}

  def draw(game: GameData, dlinkIn: Map[String, DlinkData]): Unit

}
