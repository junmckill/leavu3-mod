package se.gigurra.leavu3.app

import se.gigurra.leavu3.datamodel.{Configuration, DlinkConfiguration, DlinkData, GameData}
import se.gigurra.leavu3.interfaces.{DcsRemote, KeyPress, MouseClick}

/**
  * Created by kjolh on 3/12/2016.
  */
abstract class Instrument(configuration: Configuration) {
  def update(game: GameData, dlink: Map[String, DlinkData]): Unit
  def keyPressed(press: KeyPress): Unit
  def mouseClicked(click: MouseClick): Unit
}
