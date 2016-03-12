package se.gigurra.leavu3.gfx

import com.badlogic.gdx.graphics.Color
import se.gigurra.leavu3.Configuration
import se.gigurra.leavu3.externaldata.{Vec3, Vec2}

trait GdxImplicits { _: RenderContext.type =>

  implicit class RichGdxColor(val color: Color) {
    def scaleAlpha(s: Float): Color = new Color(color.r, color.g, color.b, color.a * s)
  }

  implicit class DrawableString(text: String) {
    def draw(wOffs: Float = 0.5f, hOffs: Float = 0.5f, color: Color = null): Unit = {
      if (color != null)
        font.setColor(color)
      font.draw(batch, text, width * (wOffs - 1.0f), height * hOffs)
    }
    def drawNextToTarget(target: Vec3, color: Color = null)(implicit configuration: Configuration): Unit = {
      transform(_
        .translate((target - self.position).withZeroZ)
        .scalexy(0.05f * symbolScale.toFloat / font.size)
        .rotate(-self.heading)) {
        draw(wOffs = 1.75f, color = color)
      }
    }
    def width: Float = font.widthOf(text)
    def height: Float = font.heightOf(text)
  }

}
