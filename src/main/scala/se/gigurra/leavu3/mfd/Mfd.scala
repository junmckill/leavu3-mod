package se.gigurra.leavu3.mfd

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Gdx.gl
import com.badlogic.gdx.graphics.g2d.{SpriteBatch, BitmapFont}
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.{OrthographicCamera, Color, GL20}
import com.badlogic.gdx.graphics.GL20._
import com.badlogic.gdx.graphics.Color._
import com.badlogic.gdx.utils.Align
import se.gigurra.leavu3.externaldata.{DlinkOutData, DlinkInData, ExternalData, GameData}
import se.gigurra.leavu3.gfx.{RichGlyphLayout, Font}
import se.gigurra.leavu3.math.Matrix4Stack

case class Mfd() {
  val shapeRenderer = new ShapeRenderer()
  val camera = new OrthographicCamera(1.0f, 1.0f)
  val batch = new SpriteBatch
  val font = Font.fromTtfFile("fonts/pt-mono/PTM55FT.ttf", size = 40)
  val transform = new Matrix4Stack(32, { t =>
    batch.setTransformMatrix(t)
    shapeRenderer.setTransformMatrix(t)
  })
  font.setColor(RED)

  def update(game: GameData, dlinkIn: DlinkInData, dlinkOut: DlinkOutData): Unit = frame {

    val text =
      s"""
         |Heading: ${game.flightModel.trueHeading.toDegrees}
         |Velocity: ${game.flightModel.velocity}""".stripMargin

    transform(_.scalexy(1.0f / text.width)) {
      text.draw()
    }
  }

  implicit class DrawableString(text: String) {
    def draw(wOffs: Float = 0.5f, hOffs: Float = 0.5f): Unit = font.draw(batch, text, width * (wOffs - 1.0f), height * hOffs)
    def width: Float = font.widthOf(text)
    def height: Float = font.heightOf(text)
  }

  def frame(f: => Unit): Unit = {
    gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
    gl.glClear(GL_COLOR_BUFFER_BIT)
    make11OrthoCamera()
    batch.begin()
    transform(_.loadIdentity()) {
      f
    }
    batch.end()
  }

  def make11OrthoCamera(scale: Float = 1.0f): Unit = {

    val w = Gdx.graphics.getWidth.toFloat
    val h = Gdx.graphics.getHeight.toFloat

    if (w > h) {
      camera.viewportWidth = scale * 2.0f * w / h
      camera.viewportHeight = scale * 2.0f
    } else {
      camera.viewportHeight = scale * 2.0f * h / w
      camera.viewportWidth = scale * 2.0f
    }

    camera.position.set(0.0f, 0.0f, 0.0f)
    camera.update()
    batch.setProjectionMatrix(camera.combined)
    shapeRenderer.setProjectionMatrix(camera.combined)
  }

}
