package se.gigurra.leavu3.gfx

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Gdx.gl
import com.badlogic.gdx.graphics.GL20._

trait RenderHelpers { _: RenderContext.type =>

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
