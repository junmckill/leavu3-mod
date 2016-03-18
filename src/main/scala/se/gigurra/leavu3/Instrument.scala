package se.gigurra.leavu3

import se.gigurra.leavu3.externaldata.{KeyPress, GameData}

/**
  * Created by kjolh on 3/12/2016.
  */
abstract class Instrument(config: Configuration, dlinkSettings: DlinkSettings) {
  def update(game: GameData, dlink: Map[String, DlinkData]): Unit
  def keyPressed(press: KeyPress): Unit
}
