package se.gigurra.leavu3.mfd

import se.gigurra.leavu3.datamodel.{Configuration, DlinkData, GameData}
import se.gigurra.leavu3.gfx.RenderContext._

/**
  * Created by kjolh on 3/12/2016.
  */
case class SmsPage(implicit config: Configuration) extends Page("SMS") {

  implicit val p = screenProjection

  def draw(game: GameData, dlinkIn: Seq[(String, DlinkData)]): Unit = {
    at(0.0, 0.85) {
      "STORES".drawCentered(WHITE, 1.5f)
    }
  }

}
