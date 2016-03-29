package se.gigurra.leavu3.gfx

import java.io.Closeable

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter
import com.badlogic.gdx.graphics.g2d.{BitmapFont, GlyphLayout, PixmapPacker}
import com.badlogic.gdx.utils.Align
import se.gigurra.leavu3.util.Decorated
import RenderContext._

case class Font(font: BitmapFont, size: Float)
  extends Decorated[BitmapFont](font)
  with Closeable {

  def prep(str: CharSequence,
           align: Int = Align.left,
           targetWidth: Float = 0.0f,
           wrap: Boolean = false,
           color: Color = null,
           alphaScale: Float = 1.0f): RichGlyphLayout = {
    new RichGlyphLayout(new GlyphLayout(
      font,
      str,
      Option(color).getOrElse(font.getColor).scaleAlpha(alphaScale),
      targetWidth,
      align,
      wrap), this)
  }

  def widthOf(lines: Seq[String]): Float = {
    val longestLine = if (lines.nonEmpty) lines.maxBy(_.length) else ""
    font.getSpaceWidth * longestLine.length.toFloat
  }

  def heightOf(lines: Seq[String]): Float = {
    val numLines = lines.size
    val heightPerLine = size
      heightPerLine * numLines
  }

  def lineHeight: Float = {
    font.getLineHeight
  }

  override def close(): Unit = {
    // Nothing really cached yet..
  }

}

object Font {


  /**
   * See https://github.com/libgdx/libgdx/wiki/Gdx-freetype
   * For parameter descriptions
   */
  def fromTtfFile(filePath: String,
                  size: Int = 20,
                  color: Color = Color.WHITE,
                  borderWidth: Float = 0.0f,
                  borderColor: Color = Color.BLACK,
                  borderStraight: Boolean = false,
                  shadowOffsetX: Int = 0,
                  shadowOffsetY: Int = 0,
                  shadowColor: Color = new Color(0, 0, 0, 0.75f),
                  characters: String = FreeTypeFontGenerator.DEFAULT_CHARS,
                  kerning: Boolean = true,
                  packer: PixmapPacker = null,
                  flip: Boolean = false,
                  genMipMaps: Boolean = false,
                  minFilter: TextureFilter = TextureFilter.Linear,
                  magFilter: TextureFilter = TextureFilter.Linear): Font = {

    val generator = new FreeTypeFontGenerator(Gdx.files.internal(filePath))
    val parameter = new FreeTypeFontParameter()

    parameter.size = size

    parameter.color = color
    parameter.borderWidth = borderWidth
    parameter.borderColor = borderColor
    parameter.borderStraight = borderStraight
    parameter.shadowOffsetX = shadowOffsetX
    parameter.shadowOffsetY = shadowOffsetY
    parameter.shadowColor = shadowColor
    parameter.characters = characters
    parameter.kerning = kerning
    parameter.packer = packer
    parameter.flip = flip
    parameter.genMipMaps = genMipMaps
    parameter.minFilter = minFilter
    parameter.magFilter = magFilter

    val font = generator.generateFont(parameter)
    font.setUseIntegerPositions(false)
    generator.dispose()
    new Font(font, size.toFloat)
  }
}
