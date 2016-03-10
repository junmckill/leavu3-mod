package se.gigurra.leavu3.externaldata

/**
  * Created by kjolh on 3/10/2016.
  */
object ExternalData {
  @volatile var gameData: GameData     = new GameData(Map.empty)
  @volatile var dlinkIn: DlinkInData   = new DlinkInData(Map.empty)
  @volatile var dlinkOut: DlinkOutData = new DlinkOutData(Map.empty)
}
