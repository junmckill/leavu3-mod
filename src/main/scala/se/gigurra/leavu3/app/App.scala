package se.gigurra.leavu3.app

import com.badlogic.gdx.{ApplicationAdapter, InputAdapter, InputProcessor}
import se.gigurra.leavu3.datamodel.Configuration
import se.gigurra.leavu3.interfaces._
import se.gigurra.serviceutils.twitter.logging.Logging

import scala.util.{Failure, Success, Try}
import scala.language.implicitConversions

/**
  * Created by kjolh on 3/20/2016.
  */
case class App(appCfg: Configuration, onCreate: () => Unit) extends ApplicationAdapter with Logging {

  val dcsRemote = DcsRemote(appCfg)
  val instrumentClassName = appCfg.instrument
  val instrumentClass: Class[Instrument] =
    Try(Class.forName(instrumentClassName)) match {
      case Success(cls) =>
        cls.asInstanceOf[Class[Instrument]]
      case Failure(e) =>
        logger.error(s"Could not find instrument $instrumentClassName - Check your spelling")
        throw e
    }
  logger.info(s"Creating instrument: $instrumentClass")
  lazy val instrument = instrumentClass.getConstructor(classOf[DcsRemote], classOf[Configuration]).newInstance(dcsRemote, appCfg)

  override def render(): Unit = {
    while(!Keyboard.inputQue.isEmpty)
      instrument.keyPressed(Keyboard.inputQue.poll)
    instrument.update(GameIn.snapshot, Dlink.In.snapshot)
  }

  override def create(): Unit = {
    onCreate()
  }

  val input = new InputAdapter {
    override def touchDown(x: Int, y: Int, pointer: Int, button: Int): Boolean = {
      instrument.mouseClicked(MouseClick(x,y,button))
      true
    }
  }

}

object App {
  implicit def toInputAdapter(app: App): InputProcessor = app.input
}
