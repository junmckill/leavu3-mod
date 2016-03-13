package se.gigurra.leavu3.gfx

import com.badlogic.gdx.graphics.Color
import se.gigurra.leavu3.Configuration
import se.gigurra.leavu3.externaldata.{Vec3, Vec2}

trait GdxImplicits { _: RenderContext.type =>

  implicit class RichGdxColor(val color: Color) {
    def scaleAlpha(s: Float): Color = new Color(color.r, color.g, color.b, color.a * s)
  }

  implicit class DrawableString(text: String) {
    private def draw(xAlign: Float = 0.0f,
             yAlign: Float = 0.0f,
             xRawOffs: Float = 0.0f,
             yRawOffs: Float = 0.0f,
             color: Color = null): Unit = {
      if (color != null)
        font.setColor(color)
      font.draw(
        batch,
        text,
        width * (xAlign - 0.5f) + xRawOffs,
        height * (yAlign + 0.5f) + yRawOffs
      )
    }

    def drawCentered(color: Color = null, scale: Float = 1.0f)(implicit configuration: Configuration): Unit = {
      transform(_
        .scalexy(scale * 0.05f * symbolScaleF / font.size)
        .rotate(-self.heading)) {
        draw(color = color, yAlign = -0.1f)
      }
    }

    def drawRightOf(color: Color = null, scale: Float = 1.0f)(implicit configuration: Configuration): Unit = {
      transform(_
        .scalexy(scale * 0.05f * symbolScaleF / font.size)
        .rotate(-self.heading)) {
        draw(xAlign = 0.5f, xRawOffs = 0.025f * symbolScaleF, color = color)
      }
    }

    def drawLeftOf(color: Color = null, scale: Float = 1.0f)(implicit configuration: Configuration): Unit = {
      transform(_
        .scalexy(scale * 0.05f * symbolScaleF / font.size)
        .rotate(-self.heading)) {
        draw(xAlign = -0.5f, xRawOffs = -0.025f * symbolScaleF, color = color)
      }
    }

    def drawBelow(color: Color = null, scale: Float = 1.0f)(implicit configuration: Configuration): Unit = {
      transform(_
        .scalexy(scale * 0.05f * symbolScaleF / font.size)
        .rotate(-self.heading)) {
        draw(yAlign = -0.5f, yRawOffs = -0.025f * symbolScaleF, color = color)
      }
    }

    def width: Float = font.widthOf(text)
    def height: Float = font.heightOf(text)
  }

}
