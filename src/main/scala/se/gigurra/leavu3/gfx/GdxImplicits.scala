package se.gigurra.leavu3.gfx

import com.badlogic.gdx.graphics.Color

trait GdxImplicits { _: RenderContext.type =>

  implicit class RichGdxColor(val color: Color) {
    def scaleAlpha(s: Float): Color = new Color(color.r, color.g, color.b, color.a * s)
  }

  implicit class DrawableString(text: String) {
    def draw(wOffs: Float = 0.5f, hOffs: Float = 0.5f): Unit = font.draw(batch, text, width * (wOffs - 1.0f), height * hOffs)
    def width: Float = font.widthOf(text)
    def height: Float = font.heightOf(text)
  }

}
