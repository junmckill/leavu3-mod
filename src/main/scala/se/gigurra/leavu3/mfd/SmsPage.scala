package se.gigurra.leavu3.mfd

import se.gigurra.leavu3.datamodel.{Configuration, DlinkData, GameData}

/**
  * Created by kjolh on 3/12/2016.
  */
case class SmsPage(implicit config: Configuration) extends Page("SMS") {

  def draw(game: GameData, dlinkIn: Map[String, DlinkData]): Unit = {

  }
}
