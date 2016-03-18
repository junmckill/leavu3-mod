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

}

object Key {
  val SHIFT = 0x10
  val LSHIFT = 0xA0
  val RSHIFT = 0xA1
  val CONTROL = 0x11
  val LCONTROL = 0xA2
  val RCONTROL = 0xA3
  val ALT = 0x12
  val LEFT = 0x25
  val RIGHT = 0x27
  val UP = 0x26
  val DOWN = 0x28
  val INSERT = 0x2D
  val DELETE = 0x2E
  val HOME = 0x24
  val END = 0x23
  val PAGE_UP = 0x21
  val PAGE_DOWN = 0x22
  val A = 65
  val B = 66
  val C = 67
  val D = 68
  val E = 69
  val F = 70
  val G = 71
  val H = 72
  val I = 73
  val J = 74
  val K = 75
  val L = 76
  val M = 77
  val N = 78
  val O = 79
  val P = 80
  val Q = 81
  val R = 82
  val S = 83
  val T = 84
  val U = 85
  val V = 86
  val W = 87
  val X = 88
  val Y = 89
  val Z = 90
}

case class KeyPress(key: Int, keysDown: Set[Int]) {
  def isKeyDown(key: Int): Boolean = keysDown.contains(key)
  def isShiftDown: Boolean = isKeyDown(Key.LSHIFT) || isKeyDown(Key.RSHIFT) || isKeyDown(Key.SHIFT)
  def isControlDown: Boolean = isKeyDown(Key.LCONTROL) || isKeyDown(Key.RCONTROL) || isKeyDown(Key.CONTROL)
  def isAltDown: Boolean = isKeyDown(Key.ALT)
}

case class Combination(key: Int, modifierTest: KeyPress => Boolean = _ => true) {
  def unapply(keyPress: KeyPress): Boolean = {
    modifierTest(keyPress) && keyPress.key == key
  }
}
