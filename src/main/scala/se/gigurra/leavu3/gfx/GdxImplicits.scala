package se.gigurra.leavu3.gfx

import com.badlogic.gdx.graphics.Color

trait GdxImplicits {

  implicit class RichGdxColor(val color: Color) {
    def scaleAlpha(s: Float): Color = new Color(color.r, color.g, color.b, color.a * s)
  }

}

object GfxImplicitImports extends GdxImplicits {

}
