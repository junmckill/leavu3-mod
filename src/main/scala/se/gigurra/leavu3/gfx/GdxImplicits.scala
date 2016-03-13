package se.gigurra.leavu3.gfx

import com.badlogic.gdx.graphics.Color
import se.gigurra.leavu3.Configuration
import se.gigurra.leavu3.externaldata.{Vec3, Vec2}

trait GdxImplicits { _: RenderContext.type =>

  implicit class RichGdxColor(val color: Color) {
    def scaleAlpha(s: Float): Color = new Color(color.r, color.g, color.b, color.a * s)
  }

  implicit class DrawableString(text: String) {
    def draw(xAlign: Float = 0.0f,
             yAlign: Float = 0.0f,
             xCharOffs: Float = 0.0f,
             yCharOffs: Float = 0.0f,
             color: Color = null): Unit = {
      if (color != null)
        font.setColor(color)
      font.draw(
        batch,
        text,
        width * (xAlign - 0.5f) + xCharOffs * font.getSpaceWidth,
        height * (yAlign + 0.5f) + yCharOffs * font.size
      )
    }
    def drawRightOf(color: Color = null)(implicit configuration: Configuration): Unit = {
      transform(_
        .scalexy(0.05f * symbolScale.toFloat / font.size)
        .rotate(-self.heading)) {
        draw(xAlign = 0.5f, color = color)
      }
    }

    def drawLeftOf(color: Color = null)(implicit configuration: Configuration): Unit = {
      transform(_
        .scalexy(0.05f * symbolScale.toFloat / font.size)
        .rotate(-self.heading)) {
        draw(xAlign = -0.5f, color = color)
      }
    }

    def drawBelow(color: Color = null)(implicit configuration: Configuration): Unit = {
      transform(_
        .scalexy(0.05f * symbolScale.toFloat / font.size)
        .rotate(-self.heading)) {
        draw(yAlign = -0.5f, color = color)
      }
    }

    def width: Float = font.widthOf(text)
    def height: Float = font.heightOf(text)
  }

}
