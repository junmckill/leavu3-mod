package se.gigurra.leavu3

import com.badlogic.gdx.backends.lwjgl.{LwjglApplication, LwjglApplicationConfiguration}
import se.gigurra.leavu3.externaldata.{DlinkOutData, DlinkInData, GameData}
import se.gigurra.serviceutils.json.JSON
import se.gigurra.serviceutils.twitter.logging.Logging

object DesktopMain extends Logging {

  def main(args: Array[String]): Unit = {
    val config = loadConfig(args.headOption.getOrElse("leavu3-cfg.json"))
    val lwjglConfig = loadLwjglConfig(config)
    new LwjglApplication(new GdxAppListener(config))

    GameData.startPoller(config.gameDataFps, config.restAddress, config.restPort)
    DlinkInData.startPoller(config.dlinkInFps, config.restAddress, config.restPort)
    DlinkOutData.startPoller(config.dlinkOutFps, config.restAddress, config.restPort)
  }

  private def loadLwjglConfig(config: Configuration): LwjglApplicationConfiguration = {
    new LwjglApplicationConfiguration {
      title = config.title
      x = config.x
      y = config.y
      width = config.width
      height = config.height
      forceExit = config.forceExit
      vSyncEnabled = config.vSyncEnabled
      foregroundFPS = config.foregroundFPS.toInt
      backgroundFPS = config.backgroundFPS.toInt
    }
  }

  private def loadConfig(path: String): Configuration = {
    val config = Configuration.readFromFile(path)
    logger.info(s"Config:\n ${JSON.write(config)}")
    config
  }

}
