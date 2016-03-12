package se.gigurra.leavu3

import se.gigurra.leavu3.externaldata.{DlinkOutData, DlinkInData, GameData}

/**
  * Created by kjolh on 3/12/2016.
  */
abstract class Instrument(config: Configuration) {
  def update(game: GameData, dlinkIn: DlinkInData, dlinkOut: DlinkOutData): Unit
}
