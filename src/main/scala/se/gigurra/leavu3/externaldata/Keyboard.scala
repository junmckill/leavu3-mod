package se.gigurra.leavu3.externaldata

import java.util.concurrent.ConcurrentLinkedQueue

import com.twitter.finagle.FailedFastException
import se.gigurra.leavu3.Configuration
import se.gigurra.leavu3.util.{RestClient, SimpleTimer}
import se.gigurra.serviceutils.json.JSON
import se.gigurra.serviceutils.twitter.logging.Logging

import scala.util.{Failure, Success, Try}

/**
  * Created by kjolh on 3/18/2016.
  */
object Keyboard extends Logging {

  val inputQue = new ConcurrentLinkedQueue[KeyPress]

  def startPolling(configuration: Configuration): Unit = {

    val client = RestClient(configuration.dcsRemoteAddress, configuration.dcsRemotePort)
    var oldKeysPressed = Set.empty[Int]

    SimpleTimer.fromFps(configuration.gameDataFps) {
      Try {
        val kbData = client.getBlocking("keyboard", cacheMaxAgeMillis = Some(Int.MaxValue))
        val keysPressed = JSON.readMap(kbData).keys.map(_.toInt).toSet
        if (keysPressed != oldKeysPressed)
          for (press <- keysPressed -- oldKeysPressed) {
            val event = KeyPress(press, keysPressed)
            inputQue.add(event)
          }
        oldKeysPressed = keysPressed
      } match {
        case Success(_) =>
        case Failure(e: FailedFastException) => // Ignore ..
        case Failure(e) => logger.warning(s"Unable to read keyboard state from dcs remote: $e")
      }
    }
  }

  val SHIFT = 0x10
  val CONTROL = 0x11
  val ALT = 0x12
}

case class KeyPress(key: Int, keysHeld: Set[Int]) {
  import Keyboard._
  def isKeyDown(key: Int): Boolean = keysHeld.contains(key)
  def isShiftDown: Boolean = isKeyDown(SHIFT)
  def isControlDown: Boolean = isKeyDown(CONTROL)
  def isAltDown: Boolean = isKeyDown(ALT)
}
