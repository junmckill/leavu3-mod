package se.gigurra.leavu3

import com.badlogic.gdx.ApplicationListener
import se.gigurra.leavu3.externaldata.ExternalData
import se.gigurra.leavu3.mfd.Mfd

case class GdxAppListener(initialConfiguration: Configuration) extends ApplicationListener {

  lazy val mfd = new Mfd()

  override def resize(width: Int, height: Int): Unit = {
  }

  override def dispose(): Unit = {
  }

  override def pause(): Unit = {
  }

  override def render(): Unit = {
    mfd.update(ExternalData.gameData, ExternalData.dlinkIn, ExternalData.dlinkOut)
  }

  override def resume(): Unit = {
  }

  override def create(): Unit = {
  }
}
