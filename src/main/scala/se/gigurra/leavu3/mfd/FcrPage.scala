package se.gigurra.leavu3.mfd

import se.gigurra.leavu3.datamodel.{Configuration, DlinkData, GameData}

/**
  * Created by kjolh on 3/12/2016.
  */
case class FcrPage(implicit config: Configuration) extends Page("FCR", config) {

  def draw(game: GameData, dlinkIn: Map[String, DlinkData]): Unit = {

  }
}
