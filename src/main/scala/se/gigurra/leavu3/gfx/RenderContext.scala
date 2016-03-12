package se.gigurra.leavu3.gfx

import com.badlogic.gdx.graphics.Color._
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import se.gigurra.leavu3.math.Matrix4Stack

object RenderContext extends GdxImplicits with RenderHelpers {
  val shapeRenderer = new ShapeRenderer()
  val camera = new OrthographicCamera(1.0f, 1.0f)
  val batch = new SpriteBatch
  val font = Font.fromTtfFile("fonts/pt-mono/PTM55FT.ttf", size = 40)
  val transform = new Matrix4Stack(32, { t =>
    batch.setTransformMatrix(t)
    shapeRenderer.setTransformMatrix(t)
  })
  font.setColor(RED)
}
