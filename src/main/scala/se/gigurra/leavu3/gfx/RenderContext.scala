package se.gigurra.leavu3.gfx

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import se.gigurra.leavu3.datamodel.Configuration
import se.gigurra.leavu3.lmath.{Matrix4Stack, RichContact, VectorImplicits}

import scala.collection.mutable

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

  val shapeRenderer = new ShapeRenderer()
  val camera = new OrthographicCamera(1.0f, 1.0f)
  val batch = new SpriteBatch
  val font = Font.fromTtfFile("fonts/pt-mono/PTM55FT.ttf", size = 40)
  val transform = new Matrix4Stack(32, { t =>
    batch.setTransformMatrix(t)
    shapeRenderer.setTransformMatrix(t)
  })
  font.setColor(RED)

  def symbolScale(implicit config: Configuration, _p: Projection[_]) = config.symbolScale * screen2World

  def symbolScaleF(implicit config: Configuration, _p: Projection[_]) = symbolScale

  val self = se.gigurra.leavu3.gfx.self

}
