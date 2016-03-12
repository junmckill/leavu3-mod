package se.gigurra.leavu3.gfx

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Gdx.gl
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20._
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.math.{Vector2, Vector3}
import se.gigurra.leavu3.externaldata.{Vec3, ExternalData}
import se.gigurra.leavu3.math.UnitConversions

trait RenderHelpers extends UnitConversions { _: RenderContext.type =>

  def LINE = ShapeType.Line
  def FILL = ShapeType.Filled
  def DOT = ShapeType.Point

  def frame(f: => Unit): Unit = {
    gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
    gl.glClear(GL_COLOR_BUFFER_BIT)
    make11OrthoCamera()
    transform(_.loadIdentity()) {
      f
    }
  }

  def batched(f: => Unit): Unit = {
    try {
      batch.begin()
      f
    } finally {
      batch.end()
    }
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

  def ppi_viewport(viewportSize: Double, heading: Float = 0.0f, offs: Vector2 = Vector2.Zero)(f: => Unit) {
    transform(_
      .translate(offs.x, offs.y, 0.0f)
      .scalexy(2.0f / viewportSize.toFloat)
      .rotate(heading)) {
      f
    }
  }

  def shape(typ: ShapeRenderer.ShapeType = LINE,
            color: Color = null)(drawCode: => Unit): Unit = {
    try {
      shapeRenderer.begin(typ)
      if (color != null)
        shapeRenderer.setColor(color)
      drawCode
    } finally {
      shapeRenderer.end()
    }
  }

  def circle(radius: Number,
             at: Vector2 = Vector2.Zero,
             steps: Int = 50,
             typ: ShapeRenderer.ShapeType = LINE,
             color: Color = null): Unit = {
    shape(typ, color) {
      shapeRenderer.circle(at.x, at.y, radius.floatValue, steps)
    }
  }

  def lines(dashes: Seq[(Vector2, Vector2)], color: Color = null): Unit = {
    shape(LINE, color) {
      for ((p1, p2) <- dashes) {
        shapeRenderer.line(p1, p2)
      }
    }
  }

  object self {
    def pitch: Float = ExternalData.gameData.selfData.pitch
    def roll: Float = ExternalData.gameData.selfData.roll
    def heading: Float = ExternalData.gameData.selfData.heading
    def position: Vec3 = ExternalData.gameData.selfData.position
    def velocity: Vec3 = ExternalData.gameData.flightModel.velocity
    def acceleration: Vec3 = ExternalData.gameData.flightModel.acceleration
  }
}
