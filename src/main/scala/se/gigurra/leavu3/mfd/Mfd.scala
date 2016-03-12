package se.gigurra.leavu3.mfd

import se.gigurra.leavu3.externaldata.{DlinkInData, DlinkOutData, GameData}
import se.gigurra.leavu3.gfx.RenderContext._

case class Mfd() {

  def update(game: GameData, dlinkIn: DlinkInData, dlinkOut: DlinkOutData): Unit = frame {

    val text =
      s"""
         |Heading: ${game.flightModel.trueHeading}
         |Velocity: ${game.flightModel.velocity}""".stripMargin

    transform(_.scalexy(1.0f / text.width)) {
      text.draw()
    }
  }

}
