package se.gigurra.leavu3

import javax.swing.JOptionPane

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl.{LwjglApplication, LwjglApplicationConfiguration}
import se.gigurra.leavu3.externaldata._
import se.gigurra.leavu3.util.RestClient
import se.gigurra.leavu3.windowstweaks.WindowTweaks
import se.gigurra.serviceutils.json.JSON
import se.gigurra.serviceutils.twitter.logging.{Capture, Logging}

import scala.util.{Failure, Success, Try}

object DesktopMain extends Logging {

  def main(args: Array[String]): Unit = Try {

    Capture.stdOutToFile(s"leavu3-debug-log.txt", append = true)
    Capture.stdErrToFile(s"leavu3-log.txt", append = true)

    val config = loadConfig(args.headOption.getOrElse("leavu3-cfg.json"))
    val dlinkConfig = downloadDlinkConfig(config)
    val lwjglConfig = loadLwjglConfig(config)
    val appListener = new GdxAppListener(config, dlinkConfig, () => onInitDisplay(config))
    new LwjglApplication(appListener, lwjglConfig)
    Gdx.input.setInputProcessor(appListener)

    ScriptInjector.startInjecting(config.dcsRemoteAddress, config.dcsRemotePort)
    GameData.startPoller(config.gameDataFps, config.dcsRemoteAddress, config.dcsRemotePort)
    DlinkOutData.startPoller(config, dlinkConfig)
    DlinkInData.startPoller(dlinkConfig)
    Keyboard.startPolling(config)

  } match {
    case Success(_) =>
    case Failure(e) =>
      JOptionPane.showMessageDialog(null, e.getMessage, s"Leavu 3 failed", JOptionPane.ERROR_MESSAGE)
      logger.error(e, s"Leavu 3 failed")
      System.exit(1)
  }

  private def onInitDisplay(config: Configuration): Unit = {
    WindowTweaks.apply(config)
  }

  private def loadLwjglConfig(config: Configuration): LwjglApplicationConfiguration = {
    System.setProperty("org.lwjgl.opengl.Window.undecorated", config.borderless.toString)
    new LwjglApplicationConfiguration {
      title = s"${config.title} (${config.instrument.split('.').last})"
      x = config.x
      y = config.y
      width = config.width
      height = config.height
      forceExit = config.forceExit
      vSyncEnabled = config.vSyncEnabled
      foregroundFPS = config.foregroundFPS.toInt
      backgroundFPS = config.backgroundFPS.toInt
      samples = config.aaSamples
      resizable = !config.borderless
    }
  }

  private def loadConfig(path: String): Configuration = {
    val config = Configuration.readFromFile(path)
    logger.info(s"Config:\n ${JSON.write(config)}")
    config
  }

  private def downloadDlinkConfig(config: Configuration): DlinkSettings = {
    logger.info(s"Downloading datalink settings from dcs-remote ..")

    val client = RestClient(config.dcsRemoteAddress, config.dcsRemotePort)
    Try(client.getBlocking(s"static-data/dlink-settings")) match {
      case Success(data) =>
        val out = JSON.read[DlinkSettings](data)
        logger.info(s"Dlink settings downloaded:\n ${JSON.write(out)}")
        out
      case Failure(e) =>
        val message = s"Could not connect to dcs-remote"
        logger.error(message)
        throw new RuntimeException(message, e)
    }
  }

}
