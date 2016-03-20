package se.gigurra.leavu3

import se.gigurra.leavu3.externaldata.{DlinkData, GameData}
import se.gigurra.leavu3.interfaces.{KeyPress, MouseClick}

/**
  * Created by kjolh on 3/12/2016.
  */
abstract class Instrument(config: Configuration, dlinkSettings: DlinkSettings) {
  def update(game: GameData, dlink: Map[String, DlinkData]): Unit
  def keyPressed(press: KeyPress): Unit
  def mouseClicked(click: MouseClick): Unit
}
