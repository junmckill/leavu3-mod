package se.gigurra.leavu3.mfd

import se.gigurra.leavu3.interfaces.Combination.ModifierTest
import se.gigurra.leavu3.interfaces.{Combination, Key, KeyPress}

import scala.collection.mutable.ArrayBuffer
import scala.util.control.NonFatal

/**
  * Created by kjolh on 4/3/2016.
  */

object DefaultMfdKeyBindings {

  def apply() = Map(
    "OSB_0" -> "CONTROL SHIFT 0",
    "OSB_1" -> "CONTROL SHIFT 1",
    "OSB_2" -> "CONTROL SHIFT 2",
    "OSB_3" -> "CONTROL SHIFT 3",
    "OSB_4" -> "CONTROL SHIFT 4",
    "OSB_5" -> "CONTROL SHIFT 5",
    "OSB_6" -> "CONTROL SHIFT 6",
    "OSB_7" -> "CONTROL SHIFT 7",
    "OSB_8" -> "CONTROL SHIFT 8",
    "OSB_9" -> "CONTROL SHIFT 9",
    "OSB_10" -> "CONTROL ALT 0",
    "OSB_11" -> "CONTROL ALT 1",
    "OSB_12" -> "CONTROL ALT 2",
    "OSB_13" -> "CONTROL ALT 3",
    "OSB_14" -> "CONTROL ALT 4",
    "OSB_15" -> "CONTROL ALT 5",
    "OSB_16" -> "CONTROL ALT 6",
    "OSB_17" -> "CONTROL ALT 7",
    "OSB_18" -> "CONTROL ALT 8",
    "OSB_19" -> "CONTROL ALT 9",
    "NEXT_QP" -> "CONTROL SHIFT RIGHT",
    "PREV_QP" -> "CONTROL SHIFT LEFT"
  )
}

trait MfdKeyBindings {

  val OSB_0: Combination
  val OSB_1: Combination
  val OSB_2: Combination
  val OSB_3: Combination
  val OSB_4: Combination
  val OSB_5: Combination
  val OSB_6: Combination
  val OSB_7: Combination
  val OSB_8: Combination
  val OSB_9: Combination
  val OSB_10: Combination
  val OSB_11: Combination
  val OSB_12: Combination
  val OSB_13: Combination
  val OSB_14: Combination
  val OSB_15: Combination
  val OSB_16: Combination
  val OSB_17: Combination
  val OSB_18: Combination
  val OSB_19: Combination
  val NEXT_QP: Combination
  val PREV_QP: Combination

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

}

object MfdKeyBindings {

  def apply(map: Map[String, String]): MfdKeyBindings = try {

    val handledBtnNames = new ArrayBuffer[String]

    def getModifierTest(modifier: String): ModifierTest = {
      modifier.toUpperCase match {
        case "ALT"      => _.isAltDown
        case "CONTROL"  => _.isControlDown
        case "SHIFT"    => _.isShiftDown
        case _ => throw new RuntimeException(s"Unknown keybinding modifier specified: $modifier")
      }
    }

    def getKey(name: String): Int = {
      Key.getVkode(name).getOrElse(throw new RuntimeException(s"Unknown key specified '$name' - Only alpha-numeric ascii supported!"))
    }

    def bindMappingFor(btnName: String): Combination = {
      map.get(btnName) match {
        case None => Combination.UNBOUND
        case Some(mappingString) =>
          handledBtnNames += btnName
          val keys = mappingString.split(' ')
          if (keys.nonEmpty) {
            val modifierTests = keys.dropRight(1).map(getModifierTest).toSeq
            val key = getKey(keys.last)
            Combination(key, p => modifierTests.forall(_(p)))
          } else {
            Combination.UNBOUND
          }
      }
    }

    val bindings = new MfdKeyBindings {
      val OSB_0 = bindMappingFor("OSB_0")
      val OSB_1 = bindMappingFor("OSB_1")
      val OSB_2 = bindMappingFor("OSB_2")
      val OSB_3 = bindMappingFor("OSB_3")
      val OSB_4 = bindMappingFor("OSB_4")
      val OSB_5 = bindMappingFor("OSB_5")
      val OSB_6 = bindMappingFor("OSB_6")
      val OSB_7 = bindMappingFor("OSB_7")
      val OSB_8 = bindMappingFor("OSB_8")
      val OSB_9 = bindMappingFor("OSB_9")
      val OSB_10 = bindMappingFor("OSB_10")
      val OSB_11 = bindMappingFor("OSB_11")
      val OSB_12 = bindMappingFor("OSB_12")
      val OSB_13 = bindMappingFor("OSB_13")
      val OSB_14 = bindMappingFor("OSB_14")
      val OSB_15 = bindMappingFor("OSB_15")
      val OSB_16 = bindMappingFor("OSB_16")
      val OSB_17 = bindMappingFor("OSB_17")
      val OSB_18 = bindMappingFor("OSB_18")
      val OSB_19 = bindMappingFor("OSB_19")
      val NEXT_QP = bindMappingFor("NEXT_QP")
      val PREV_QP = bindMappingFor("PREV_QP")
    }

    val unhandledBtnNames = map.keys.toSet -- handledBtnNames.toSet
    if (unhandledBtnNames.nonEmpty)
      throw new RuntimeException(s"Unknown commands specified: ${unhandledBtnNames.mkString(",")}")

    bindings

  } catch {
    case NonFatal(e) =>
      throw new RuntimeException(s"Unable to load config file key bindings: \n $e", e)
  }
}

