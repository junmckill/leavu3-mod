package se.gigurra.leavu3.gfx

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import se.gigurra.leavu3.Configuration
import se.gigurra.leavu3.math.{RichContact, RichVector3Implicits, Matrix4Stack}

import scala.collection.mutable

object RenderContext
  extends GdxImplicits
    with RenderHelpers
    with RichVector3Implicits
    with RichContact
    with Colors {

  val shapeRenderer = new ShapeRenderer()
  val camera = new OrthographicCamera(1.0f, 1.0f)
  val batch = new SpriteBatch
  val font = Font.fromTtfFile("fonts/pt-mono/PTM55FT.ttf", size = 40)
  val transform = new Matrix4Stack(32, { t =>
    batch.setTransformMatrix(t)
    shapeRenderer.setTransformMatrix(t)
  })
  font.setColor(RED)

  def symbolScale(implicit config: Configuration) = config.symbolScale * screen2World
  def symbolScaleF(implicit config: Configuration) = symbolScale.toFloat


}
