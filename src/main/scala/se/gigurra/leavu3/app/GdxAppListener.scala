package se.gigurra.leavu3.app

import com.badlogic.gdx.{ApplicationListener, InputProcessor}
import se.gigurra.leavu3.datamodel.{Configuration, DlinkConfiguration}
import se.gigurra.leavu3.interfaces.{Keyboard, MouseClick, Snapshots}
import se.gigurra.serviceutils.twitter.logging.Logging

import scala.util.{Failure, Success, Try}

/**
  * Created by kjolh on 3/20/2016.
  */
case class GdxAppListener(initialConfiguration: Configuration,
                          dlinkSettings: DlinkConfiguration,
                          onCreate: () => Unit)
  extends ApplicationListener
    with InputProcessor
    with Logging {

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
  lazy val instrument = instrumentClass.getConstructor(classOf[Configuration], classOf[DlinkConfiguration]).newInstance(initialConfiguration, dlinkSettings)

  override def resize(width: Int, height: Int): Unit = {
  }

  override def dispose(): Unit = {
  }

  override def pause(): Unit = {
  }

  override def render(): Unit = {
    while(!Keyboard.inputQue.isEmpty)
      instrument.keyPressed(Keyboard.inputQue.poll)
    instrument.update(Snapshots.gameData, Snapshots.dlinkIn)
  }

  override def resume(): Unit = {
  }

  override def create(): Unit = {
    onCreate()
  }

  override def mouseMoved(screenX: Int, screenY: Int): Boolean = { false }
  override def keyTyped(character: Char): Boolean = { false }
  override def keyDown(keycode: Int): Boolean = { false }
  override def touchDown(x: Int, y: Int, pointer: Int, button: Int): Boolean = {
    instrument.mouseClicked(MouseClick(x,y,button))
    true
  }
  override def keyUp(keycode: Int): Boolean = { false }
  override def scrolled(amount: Int): Boolean = { false }
  override def touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean = { false }
  override def touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean = { false }
}
