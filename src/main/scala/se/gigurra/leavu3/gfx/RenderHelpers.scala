package se.gigurra.leavu3.gfx

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Gdx.gl
import com.badlogic.gdx.graphics.GL20._
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.math.Vector3
import se.gigurra.leavu3.externaldata.ExternalData
import se.gigurra.leavu3.math.UnitConversions

trait RenderHelpers extends UnitConversions { _: RenderContext.type =>

  def LINE = ShapeType.Line
  def FILL = ShapeType.Filled
  def DOT = ShapeType.Point

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

  case class geo_viewport(at: Vector3, viewportSize: Float, heading: Float = 0.0f) {
    def ppi(f: => Unit): Unit = {
      transform(_.loadIdentity()
        .translate(-at)
        .rotate(-heading)
        .scalexy(1.0f / viewportSize)) {
        f
      }
    }
  }

  def circle(at: Vector3, radius: Float, steps: Int = 50, typ: ShapeRenderer.ShapeType = LINE): Unit = {
    shapeRenderer.begin(typ)
    shapeRenderer.circle(at.x, at.y, radius, steps)
    shapeRenderer.end()
  }

  object self {
    def pitch: Float = ExternalData.gameData.selfData.pitch
    def roll: Float = ExternalData.gameData.selfData.roll
    def heading: Float = ExternalData.gameData.selfData.heading
    def position: Vector3 = ExternalData.gameData.selfData.position
  }
}
