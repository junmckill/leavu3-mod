package se.gigurra.leavu3.mfd


import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import se.gigurra.leavu3.app.Version
import se.gigurra.leavu3.datamodel._
import se.gigurra.leavu3.gfx.RenderContext._
import se.gigurra.leavu3.gfx.{Blink, ScreenProjection}
import se.gigurra.leavu3.interfaces.{DcsRemote, Dlink, GameIn, MouseClick}
import se.gigurra.leavu3.lmath.Box

import scala.language.postfixOps

/**
  * Created by kjolh on 3/12/2016.
  */
case class InfoPage(implicit config: Configuration) extends Page("INF") {

  implicit val projection = new ScreenProjection()
  val OSB_UPDATE_COVER = 2
  val OSB_UPDATE = 3
  val OSB_UPDATE_COVER2 = 4
  val blink = Blink(Seq(true, false), 1.0)
  val clickToUpdateText = "CLICK TO UPDATE"

  override def pressOsb(i: Int): Unit = {
    i match {
      case OSB_UPDATE_COVER => Version.downloadLatest()
      case OSB_UPDATE_COVER2 => Version.downloadLatest()
      case OSB_UPDATE => Version.downloadLatest()
      case _ =>
    }
  }

  override def mouseClicked(click: MouseClick): Unit =  {
    val center = Mfd.Osb.positions(OSB_UPDATE)
    val height = Mfd.Osb.boxHeight * config.symbolScale
    val width = Mfd.Osb.boxWidth * config.symbolScale * (clickToUpdateText.length.toFloat / 3.0f)
    val hitBox = Box(width, height, center)
    if (hitBox.contains(click.ortho11Raw)) {
      Version.downloadLatest()
    }
  }

  override def draw(game: GameData, dlinkIn: Map[String, DlinkData]): Unit = {

    val updateAvailable = Version.current != Version.latest
    val scale = config.symbolScale * 0.02 / font.getSpaceWidth

    batched { atScreen(-0.8, 0.8) {

      transform(_
        .scalexy(scale)) {

        var n = 0
        val titleLen = 16
        def drawTextLine(title: String, value: Any, color: Color): Unit = {
          transform(_.translate(y = -n.toFloat * font.getLineHeight)){
            val fullString = title.pad(titleLen) + " : " + value
            fullString.drawRaw(xAlign = 0.5f, color = color)
          }
          n += 1
        }

        def mkStringOfPlayers(names: Iterable[String]): String = {
          names.map(_.take(6)).mkString(", ")
        }

        val nMaxPerLine = 6

        def buildPlayerLists(data: Map[String, DlinkData]): Seq[String] = {
          val out = data
            .keys.toSeq
            .sorted
            .sliding(nMaxPerLine,nMaxPerLine).toSeq
            .map(mkStringOfPlayers)
          if (out.nonEmpty) {
            out
          } else {
            Seq("")
          }
        }

        val bluePlayerLists = buildPlayerLists(Dlink.In.blue)
        val redPlayerLists = buildPlayerLists(Dlink.In.red)
        val notPlayingPlayerLists = buildPlayerLists(Dlink.In.notPlaying)

        drawTextLine("-----VERSION----", "--------------------------------", LIGHT_GRAY)
        drawTextLine(" Latest version", Version.latest, LIGHT_GRAY)
        drawTextLine("   Your version", Version.current, if (updateAvailable) YELLOW else LIGHT_GRAY)
        drawTextLine("Update available", if (updateAvailable) "Yes" else "No", if (updateAvailable) YELLOW else LIGHT_GRAY)
        drawTextLine("                ", "", LIGHT_GRAY)
        drawTextLine("-----STATUS-----", "--------------------------------", LIGHT_GRAY)
        drawTextLine("      DCS Remote", if (GameIn.dcsRemoteConnected) "connected" else "disconnected", if (GameIn.dcsRemoteConnected) GREEN else RED)
        drawTextLine("        DCS Game", if (GameIn.dcsGameConnected) "connected" else "disconnected", if (GameIn.dcsGameConnected) GREEN else RED)
        drawTextLine("           DLink", if (Dlink.connected) "connected" else "disconnected", if (Dlink.connected) GREEN else RED)
        drawTextLine("        Draw fps", Gdx.graphics.getFramesPerSecond, LIGHT_GRAY)
        drawTextLine("                ", "", LIGHT_GRAY)
        drawTextLine("----SETTINGS----", "--------------------------------", LIGHT_GRAY)
        drawTextLine("      DCS Remote", s"${config.dcsRemoteAddress}:${config.dcsRemotePort}", LIGHT_GRAY)
        drawTextLine("    DLink server", s"${Dlink.config.host}:${Dlink.config.port}", LIGHT_GRAY)
        drawTextLine("      DLink team", Dlink.config.team, LIGHT_GRAY)
        drawTextLine("  DLink callsign", Dlink.config.callsign, LIGHT_GRAY)
        drawTextLine("      DLink mode", if (config.relayDlink) "receive + transmit" else "receive", LIGHT_GRAY)
        drawTextLine("     gameDataFps", config.gameDataFps, LIGHT_GRAY)
        drawTextLine("     symbolScale", config.symbolScale, LIGHT_GRAY)
        drawTextLine("                ", "", LIGHT_GRAY)
        drawTextLine("--DLINK MEMBERS-", "--------------------------------", LIGHT_GRAY)

        def drawPlayerLists(title: String, lines: Seq[String], color: Color): Unit = {
          for (line <- lines) {
            drawTextLine(title, line, color)
          }
        }
        drawPlayerLists("BLUE", bluePlayerLists, BLUE)
        drawPlayerLists("RED", redPlayerLists, RED)
        drawPlayerLists("NOT PLAYING", notPlayingPlayerLists, DARK_GRAY)
      }
    }}

    if (updateAvailable) {
      Mfd.Osb.drawHighlighted(OSB_UPDATE, clickToUpdateText, highlighted = blink)
    }

  }

}
