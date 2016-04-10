package se.gigurra.leavu3.mfd

import com.badlogic.gdx.graphics.Color
import com.google.common.base.Splitter
import se.gigurra.leavu3.datamodel.{Configuration, CounterMeasures, DlinkData, EngineIndicators, GameData, Payload, Vec2, self}
import se.gigurra.leavu3.gfx.{Blink, CountDown}
import se.gigurra.leavu3.gfx.RenderContext._
import se.gigurra.leavu3.interfaces.{DcsRemote, GameIn}

import scala.collection.JavaConversions._

/**
  * Created by kjolh on 3/12/2016.
  */
case class SmsPage(implicit config: Configuration, mfd: MfdIfc) extends Page("SMS", Page.Priorities.SMS) {

  implicit val p = screenProjection
  val yTitle = 0.85
  val top = yTitle - 0.15
  val bottom = -0.5
  val height = top - bottom

  def draw(game: GameData, dlinkIn: Seq[(String, DlinkData)]): Unit = {
    at((0.0, yTitle)) {
      "STORES".drawCentered(WHITE, 1.5f)
    }
    drawBoxPayload(game.payload)
    drawTextPayload(game.payload, game.electronicWarf.counterMeasures, game.indicators.engines)
  }

  def drawBoxPayload(payload: Payload): Unit = {

    val typ2Station =
      payload.stations.zipWithIndex
        .groupBy(_._1.typ).toSeq
        .sortBy(_._1.fullName)
        .filterNot(_._1.fullName.contains("unknown"))

    val nTypes = typ2Station.size

    val lineHeight = height / math.max(1.0, nTypes.toDouble)
    val padding = 0.25 * lineHeight
    val boxHeight = lineHeight - padding
    val boxWidth = 0.8
    val xCtr = -0.4

    val iSelected = payload.currentStation - 1

    var i = 0
    for ((typ, seqStationAndPylonIndex) <- typ2Station) {

      val pylonIndices = seqStationAndPylonIndex.map(_._2)
      val isSelected = pylonIndices.contains(iSelected)
      val head = seqStationAndPylonIndex.head._1
      val name = head.typ.fullName.take(15)
      val count = seqStationAndPylonIndex.map(_._1.count).sum
      val yCtr = top - i.toDouble * lineHeight - lineHeight / 2.0
      val color = if (isSelected) WHITE else if (count > 0) GREEN else GRAY

      at((xCtr, yCtr)) {
        rect(width = boxWidth, height = boxHeight, typ = LINE, color = color)
        val text = s" ${count.toString.pad(3)}x $name".padRight(25)
        text.drawCentered(color)
      }
      i += 1
    }

  }

  val blinkCountdownTime = 4.0
  def newCountdown(): CountDown = CountDown(blinkCountdownTime)

  val blink = Blink(Seq(true, false), 0.5)

  var gunBlinkCountdown = CountDown(0.0)
  var chaffBlinkCountdown = CountDown(0.0)
  var flareBlinkCountdown = CountDown(0.0)

  var gunLastCycle = 0
  var chaffLastCycle = 0
  var flareLastCycle = 0

  def getRightSideTextColor(count: Int, countDown: CountDown): Color = {
    countDown.isReached match {
      case true => if (count == 0) RED else GREEN
      case false => if (blink) YELLOW else if (count == 0) RED else GREEN
    }
  }

  def bingo = DcsRemote.remoteConfig.missionSettings.bingo

  def joker = DcsRemote.remoteConfig.missionSettings.joker

  def getFuelTimeStringUntil(current: Double, target: Double): (String, Color) = {

    val playtimeTotalSeconds = math.max(0.0, (current - target) / GameIn.estimatedFueldConsumption)
    val playtimeTotalMinutes = (playtimeTotalSeconds / 60.0).round

    val playtimeHours = (playtimeTotalSeconds / 3600.0).floor.round
    val playtimeMinutes = playtimeTotalMinutes - playtimeHours * 60L

    val playtimeColor = if (playtimeTotalMinutes > 10) GREEN else if (playtimeTotalMinutes > 0) YELLOW else RED
    (s"${playtimeHours.pad(2, '0')}:${playtimeMinutes.pad(2, '0')}", playtimeColor)
  }

  def drawTextPayload(payload: Payload, cms: CounterMeasures, eng: EngineIndicators): Unit = {

    val scale = config.symbolScale * 0.03 / font.getSpaceWidth

    batched {
      at((0.5, top - font.lineHeight * scale)) {
        transform(_.scalexy(scale)) {


          val leftPad = 8
          val rightPad = 22
          var n = 0

          def nextLine(): Unit = {
            n += 1
          }

          def drawTextLine(left: Any, right: String, color: Color): Unit = {
            val str = s"${left.toString.pad(leftPad)}x $right".padRight(rightPad)
            transform(_.translate(y = -n.toFloat * font.getLineHeight))(str.drawRaw(color = color))
            nextLine()
          }

          def drawFuel(name: String, value: Double, bingo: Double, joker: Double, rounding: Long = 10): Unit = {
            val m1 = 1.0 / rounding.toDouble
            val m2 = rounding
            val fuelNumber = Splitter.fixedLength(3).split(((value * kg_to_fuelUnit * m1).round*m2).toString.reverse).mkString(".").reverse
            val fuelColor = if (value < bingo) RED else if (value < joker) YELLOW else GREEN
            drawTextLine(fuelNumber, name, fuelColor)
          }

          drawTextLine(payload.cannon.shells, "Gun", getRightSideTextColor(payload.cannon.shells, gunBlinkCountdown))
          nextLine()
          drawTextLine(cms.chaff, "Chaff", getRightSideTextColor(cms.chaff, chaffBlinkCountdown))
          drawTextLine(cms.flare, "Flare", getRightSideTextColor(cms.flare, flareBlinkCountdown))
          nextLine()

          drawFuel("Fuel", eng.fuelTotal, bingo = bingo * lbs_to_kg, joker = joker * lbs_to_kg)
          drawFuel("internal", eng.fuelInternal, bingo = bingo * lbs_to_kg, joker = joker * lbs_to_kg)
          drawFuel("external", eng.fuelExternal, bingo = 1, joker = 0)

          nextLine()
          drawTextLine(s"${eng.rpm.left.round} ${eng.rpm.right.round}", "RPM", GREEN)
          drawFuel("Flow", GameIn.estimatedFueldConsumption * 3600.0, bingo = 100, joker = 1000, rounding = 100)

          def drawFuelFlow(target: Double, name: String): Unit = {
            val flow = getFuelTimeStringUntil(eng.fuelTotal, target)
            drawTextLine(flow._1, s"T -> $name", flow._2)
          }

          if (GameIn.estimatedFueldConsumption > 0.001) {
            drawFuelFlow(joker * lbs_to_kg, "Joker")
            drawFuelFlow(bingo * lbs_to_kg, "Bingo")
            drawFuelFlow(0, "Empty")
          }

          if (payload.cannon.shells < gunLastCycle)
            gunBlinkCountdown = newCountdown()

          if (cms.chaff < chaffLastCycle)
            chaffBlinkCountdown = newCountdown()

          if (cms.flare < flareLastCycle)
            flareBlinkCountdown = newCountdown()

          gunLastCycle = payload.cannon.shells
          chaffLastCycle = cms.chaff
          flareLastCycle = cms.flare

        }
      }
    }
  }

}
