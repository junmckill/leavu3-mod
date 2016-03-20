package se.gigurra.leavu3.interfaces

import se.gigurra.leavu3.externaldata.{DlinkData, GameData}

/**
  * Created by kjolh on 3/10/2016.
  */
object Snapshots {
  @volatile var gameData: GameData              = new GameData(Map.empty)
  @volatile var dlinkIn: Map[String, DlinkData] = Map.empty
}
