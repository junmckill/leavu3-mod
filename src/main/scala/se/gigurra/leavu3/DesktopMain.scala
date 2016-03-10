package se.gigurra.leavu3

import se.gigurra.serviceutils.json.JSON
import se.gigurra.serviceutils.twitter.logging.Logging

object DesktopMain extends Logging{

  def main(args: Array[String]): Unit = {
    val config = Configuration.readFromFile(args.headOption.getOrElse("leavu3-cfg.json"))
    logger.info(s"Config:\n ${JSON.write(config)}")

  }
}
