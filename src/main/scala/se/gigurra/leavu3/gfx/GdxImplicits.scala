package se.gigurra.leavu3.gfx

import com.badlogic.gdx.graphics.Color
import se.gigurra.leavu3.datamodel.{Configuration, Vec2, Vec3}

object getLines {
  def apply(s: String): Seq[String] = {
    s.lines.toSeq
  }
}

trait GdxImplicits { _: RenderContext.type =>

  implicit class RichGdxColor(val color: Color) {
    def scaleAlpha(s: Float): Color = new Color(color.r, color.g, color.b, color.a * s)
    def scaleAlpha(s: Double): Color = new Color(color.r, color.g, color.b, color.a * s.toFloat)
    def scale(r: Float = 1.0f, g: Float = 1.0f, b: Float = 1.0f, a: Float = 1.0f): Color = new Color(color.r * r, color.g * g, color.b * b, color.a * a)
  }

  implicit class DrawableSomething(x: Any) {
    def pad(len: Int, c: Char = ' '): String = {
      val res = String.valueOf(x)
      (0 until math.max(0, len - res.length)).map(_ => c).mkString + res
    }

    def padRight(len: Int, c: Char = ' '): String = {
      val res = String.valueOf(x)
      res + (0 until math.max(0, len - res.length)).map(_ => c).mkString
    }
  }

  implicit class DrawableString(text: String) {

    val lines = getLines(text)
    val extraDownOffset = -0.1f / math.max(lines.size, 1).toFloat

    def drawRaw(xAlign: Float = 0.0f,
               yAlign: Float = 0.0f,
               xRawOffs: Float = 0.0f,
               yRawOffs: Float = 0.0f,
               color: Color = null): Unit = {
      if (color != null)
        font.setColor(color)

      val doBatch = !batch.isDrawing

      if (doBatch)
        batch.begin()

      font.draw(
        batch,
        text,
        width  * (xAlign - 0.5f) + xRawOffs,
        height * (yAlign + 0.5f) + yRawOffs
      )

      if (doBatch) {
        batch.end()
        reenableBlending()
      }

    }

    def drawCentered[_: Projection](color: Color = null, scale: Float = 1.0f)(implicit configuration: Configuration): Unit = {
      transform(_
        .scalexy(scale * 0.05f * symbolScaleF / font.size)
        .rotate(projection.headingCorrection)) {
        drawRaw(color = color, yAlign = extraDownOffset)
      }
    }

    def drawRightOf[_: Projection](color: Color = null, scale: Float = 1.0f)(implicit configuration: Configuration): Unit = {
      transform(_
        .scalexy(scale * 0.05f * symbolScaleF / font.size)
        .rotate(projection.headingCorrection)) {
        drawRaw(xAlign = 0.5f, yAlign = extraDownOffset, xRawOffs = 0.025f * symbolScaleF, color = color)
      }
    }

    def drawLeftOf[_: Projection](color: Color = null, scale: Float = 1.0f)(implicit configuration: Configuration): Unit = {
      transform(_
        .scalexy(scale * 0.05f * symbolScaleF / font.size)
        .rotate(projection.headingCorrection)) {
        drawRaw(xAlign = -0.5f, yAlign = extraDownOffset, xRawOffs = -0.025f * symbolScaleF, color = color)
      }
    }

    def drawBelow[_: Projection](color: Color = null, scale: Float = 1.0f)(implicit configuration: Configuration): Unit = {
      transform(_
        .scalexy(scale * 0.05f * symbolScaleF / font.size)
        .rotate(projection.headingCorrection)) {
        drawRaw(yAlign = -0.5f, yRawOffs = -0.025f * symbolScaleF, color = color)
      }
    }

    def width: Float = font.widthOf(lines)
    def height: Float = font.heightOf(lines)
  }

}
