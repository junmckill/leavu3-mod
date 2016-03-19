package se.gigurra.leavu3.externaldata

import java.util.concurrent.ConcurrentLinkedQueue

import com.badlogic.gdx.Gdx
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
        val keysPressed = JSON.readMap(kbData).keys.map(_.toInt).toSet.map((x: Int) => x - configuration.keyBindingOffset)
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
  val LALT = 164
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
  val _0 = 48
  val _1 = 49
  val _2 = 50
  val _3 = 51
  val _4 = 52
  val _5 = 53
  val _6 = 54
  val _7 = 55
  val _8 = 56
  val _9 = 57
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
  val OSB_0 = new Combination(_0, p => p.isControlDown && p.isShiftDown)
  val OSB_1 = new Combination(_1, p => p.isControlDown && p.isShiftDown)
  val OSB_2 = new Combination(_2, p => p.isControlDown && p.isShiftDown)
  val OSB_3 = new Combination(_3, p => p.isControlDown && p.isShiftDown)
  val OSB_4 = new Combination(_4, p => p.isControlDown && p.isShiftDown)
  val OSB_5 = new Combination(_5, p => p.isControlDown && p.isShiftDown)
  val OSB_6 = new Combination(_6, p => p.isControlDown && p.isShiftDown)
  val OSB_7 = new Combination(_7, p => p.isControlDown && p.isShiftDown)
  val OSB_8 = new Combination(_8, p => p.isControlDown && p.isShiftDown)
  val OSB_9 = new Combination(_9, p => p.isControlDown && p.isShiftDown)
  val OSB_10 = new Combination(_0, p => p.isControlDown && p.isAltDown)
  val OSB_11 = new Combination(_1, p => p.isControlDown && p.isAltDown)
  val OSB_12 = new Combination(_2, p => p.isControlDown && p.isAltDown)
  val OSB_13 = new Combination(_3, p => p.isControlDown && p.isAltDown)
  val OSB_14 = new Combination(_4, p => p.isControlDown && p.isAltDown)
  val OSB_15 = new Combination(_5, p => p.isControlDown && p.isAltDown)
  val OSB_16 = new Combination(_6, p => p.isControlDown && p.isAltDown)
  val OSB_17 = new Combination(_7, p => p.isControlDown && p.isAltDown)
  val OSB_18 = new Combination(_8, p => p.isControlDown && p.isAltDown)
  val OSB_19 = new Combination(_9, p => p.isControlDown && p.isAltDown)

  object QP_OSB {
    def unapply(keyPress: KeyPress): Option[Int] = {
      keyPress match {
        case OSB_11() => Some(11)
        case OSB_12() => Some(12)
        case OSB_13() => Some(13)
        case _ => None
      }
    }
  }

  object OSB {
    def unapply(keyPress: KeyPress): Option[Int] = {
      keyPress match {
        case OSB_0() => Some(0)
        case OSB_1() => Some(1)
        case OSB_2() => Some(2)
        case OSB_3() => Some(3)
        case OSB_4() => Some(4)

        case OSB_5() => Some(5)
        case OSB_6() => Some(6)
        case OSB_7() => Some(7)
        case OSB_8() => Some(8)
        case OSB_9() => Some(9)

        case OSB_10() => Some(10)
        case OSB_11() => Some(11)
        case OSB_12() => Some(12)
        case OSB_13() => Some(13)
        case OSB_14() => Some(14)

        case OSB_15() => Some(15)
        case OSB_16() => Some(16)
        case OSB_17() => Some(17)
        case OSB_18() => Some(18)
        case OSB_19() => Some(19)
        case _ => None
      }
    }
  }

  val modifiers = Set(LSHIFT, RSHIFT, SHIFT, LCONTROL, RCONTROL, CONTROL, ALT)
}

case class MouseClick(screenX: Int, screenY: Int, button: Int) {

  val hw = Gdx.graphics.getWidth.toFloat / 2.0f
  val hh = Gdx.graphics.getHeight.toFloat / 2.0f

  val x11Raw = (screenX.toFloat - hw) / hw
  val y11Raw = -(screenY.toFloat - hh) / hh
  val ortho11Raw = Vec2(x11Raw, y11Raw)

  val (sx, sy) =
    if (hw > hh) {
      (hw / hh, 1.0f)
    } else {
      (1.0f, hh / hw)
    }

  val x11 = sx * x11Raw
  val y11 = sy * y11Raw
  val ortho11 = Vec2(x11, y11)

  override def toString: String = {
    s"MouseClick[$x11, $y11][$x11Raw, $y11Raw]"
  }

}

case class KeyPress(key: Int, keysDown: Set[Int]) {
  def isKeyDown(key: Int): Boolean = keysDown.contains(key)
  def isShiftDown: Boolean = isKeyDown(Key.LSHIFT) || isKeyDown(Key.RSHIFT) || isKeyDown(Key.SHIFT)
  def isControlDown: Boolean = isKeyDown(Key.LCONTROL) || isKeyDown(Key.RCONTROL) || isKeyDown(Key.CONTROL)
  def isAltDown: Boolean = isKeyDown(Key.ALT) || isKeyDown(Key.LALT)
  override def toString: String = {
    val char = key.toString.toInt.toChar
    val base: String =
      if (Key.modifiers.contains(key))
        ""
      else if (char.isLetterOrDigit)
        char.toString
      else
        key.toString
    s"${if (isControlDown) "Control " else ""}${if (isShiftDown) "Shift " else ""}${if (isAltDown) "Alt " else ""}$base"
  }
}

class Combination(key: Int, modifierTest: KeyPress => Boolean = _ => true) {
  def unapply(keyPress: KeyPress): Boolean = {
    modifierTest(keyPress) && keyPress.key == key
  }
}
