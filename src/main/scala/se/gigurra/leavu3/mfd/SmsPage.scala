package se.gigurra.leavu3.mfd

import se.gigurra.leavu3.datamodel.{Configuration, DlinkData, GameData, Payload, Vec2}
import se.gigurra.leavu3.gfx.RenderContext._

/**
  * Created by kjolh on 3/12/2016.
  */
case class SmsPage(implicit config: Configuration) extends Page("SMS") {

  implicit val p = screenProjection
  val yTitle = 0.85
  val top = yTitle - 0.15
  val bottom = -0.5
  val height = top - bottom

  def draw(game: GameData, dlinkIn: Seq[(String, DlinkData)]): Unit = {
    at(0.0, yTitle) {
      "STORES".drawCentered(WHITE, 1.5f)
    }
    drawBoxPayload(game.payload)
    drawTextPayload(game.payload)
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

        at(xCtr, yCtr) {
          rect(width = boxWidth, height = boxHeight, typ = LINE, color = color)
          val text = s" ${count.toString.pad(3)}x $name".padRight(25)
          text.drawCentered(color)
        }
      i += 1
    }

  }

  def drawTextPayload(payload: Payload): Unit = {

  }


}
