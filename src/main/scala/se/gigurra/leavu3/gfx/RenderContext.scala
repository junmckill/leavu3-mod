package se.gigurra.leavu3.gfx

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import se.gigurra.leavu3.datamodel.Configuration
import se.gigurra.leavu3.lmath.{Matrix4Stack, RichContact, VectorImplicits}

object RenderContext
  extends GdxImplicits
    with RenderHelpers
    with VectorImplicits
    with RichContact
    with Colors {

  def pixelWidth = Gdx.graphics.getWidth.toFloat
  def pixelHeight = Gdx.graphics.getHeight.toFloat
  def x11Scale = if (pixelWidth > pixelHeight) pixelWidth / pixelHeight else 1.0f
  def y11Scale = if (pixelWidth > pixelHeight) 1.0f else pixelHeight / pixelWidth

  lazy val shapeRenderer = new ShapeRenderer()
  lazy val camera = new OrthographicCamera(1.0f, 1.0f)
  lazy val batch = new SpriteBatch
  lazy val font = Font.fromTtfFile("fonts/pt-mono/PTM55FT.ttf", size = 40)
  lazy val transform = new Matrix4Stack(32, { t =>
    batch.setTransformMatrix(t)
    shapeRenderer.setTransformMatrix(t)
  })

  def symbolScale(implicit config: Configuration, _p: Projection[_]) = config.symbolScale * screen2World

  def symbolScaleF(implicit config: Configuration, _p: Projection[_]) = symbolScale

}
