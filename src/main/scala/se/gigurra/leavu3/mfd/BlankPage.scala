package se.gigurra.leavu3.mfd

import se.gigurra.leavu3.datamodel.{Configuration, DlinkData, GameData}

/**
  * Created by kjolh on 3/12/2016.
  */
case class BlankPage(implicit config: Configuration, mfd: MfdIfc) extends Page("") {

  def draw(game: GameData, dlinkIn: Seq[(String, DlinkData)]): Unit = {

  }
}
