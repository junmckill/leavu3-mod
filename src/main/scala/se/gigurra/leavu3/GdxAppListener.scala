package se.gigurra.leavu3

import com.badlogic.gdx.ApplicationListener
import se.gigurra.leavu3.externaldata.ExternalData
import se.gigurra.serviceutils.twitter.logging.Logging

import scala.util.{Failure, Success, Try}

case class GdxAppListener(initialConfiguration: Configuration,
                          dlinkSettings: DlinkSettings,
                          onCreate: () => Unit) extends ApplicationListener with Logging{

  val instrumentClassName = initialConfiguration.instrument
  val instrumentClass: Class[Instrument] =
    Try(Class.forName(instrumentClassName)) match {
      case Success(cls) =>
        cls.asInstanceOf[Class[Instrument]]
      case Failure(e) =>
        logger.error(s"Could not find instrument $instrumentClassName - Check your spelling")
        throw e
    }
  logger.info(s"Creating instrument: $instrumentClass")
  lazy val instrument = instrumentClass.getConstructor(classOf[Configuration], classOf[DlinkSettings]).newInstance(initialConfiguration, dlinkSettings)

  override def resize(width: Int, height: Int): Unit = {
  }

  override def dispose(): Unit = {
  }

  override def pause(): Unit = {
  }

  override def render(): Unit = {
    instrument.update(ExternalData.gameData, ExternalData.dlinkIn)
  }

  override def resume(): Unit = {
  }

  override def create(): Unit = {
    onCreate()
  }
}
