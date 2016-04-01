package se.gigurra.leavu3.gfx

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Gdx.gl
import com.badlogic.gdx.graphics.{Color, GL20}
import com.badlogic.gdx.graphics.GL20._
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.math.{Rectangle, Vector2}
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack
import se.gigurra.leavu3.datamodel._
import se.gigurra.leavu3.lmath.{NormalizeDegrees, UnitConversions}

import scala.language.implicitConversions

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
      reenableBlending()
    }
  }

  def reenableBlending(): Unit = {
    Gdx.graphics.getGL20.glEnable(GL20.GL_BLEND)
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
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

  def shape(typ: ShapeRenderer.ShapeType = LINE, color: Color = null)(drawCode: => Unit): Unit = {
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
             steps: Int = 50,
             typ: ShapeRenderer.ShapeType = LINE,
             color: Color = null): Unit = {
    shape(typ, color) {
      shapeRenderer.circle(0,0,radius.floatValue, steps)
    }
  }

  def rect(width: Number = 1.0,
           height: Number = 1.0,
           at: Vector2 = Vector2.Zero,
           typ: ShapeRenderer.ShapeType = LINE,
           color: Color = null): Unit = {
    shape(typ, color) {
      shapeRenderer.rect(at.x - width.floatValue / 2.0f, at.y - height.floatValue / 2.0f, width.floatValue, height.floatValue)
    }
  }

  def triangle(a: Vec2,
               b: Vec2,
               c: Vec2,
               at: Vector2 = Vector2.Zero,
               typ: ShapeRenderer.ShapeType = LINE,
               color: Color = null): Unit = {
    shape(typ, color) {
      shapeRenderer.triangle(
        at.x + a.x.toFloat,
        at.y + a.y.toFloat,
        at.x + b.x.toFloat,
        at.y + b.y.toFloat,
        at.x + c.x.toFloat,
        at.y + c.y.toFloat
      )
    }
  }

  def arc(radius: Number,
          angle: Number,
          at: Vector2 = Vector2.Zero,
          steps: Int = 50,
          direction: Number = 0.0f,
          color: Color = null): Unit = {
    shape(LINE, color) {
      shapeRenderer.arc(at.x, at.y, radius.floatValue, -angle.floatValue/2.0f + 90.0f - direction.floatValue, angle.floatValue, steps)
    }
  }

  def lineBetween[_: Projection](aWorld: Vec3, bWorld: Vec3, color: Color, scaleOut: Double = 1.0, scaleIn: Double = 1.0): Unit = {
    val p0 = prePosition(aWorld): Vec2
    val p1 = prePosition(bWorld): Vec2
    val delta = p1 - p0
    val a = p1 - delta * scaleIn
    val b = p0 + delta * scaleOut
    lines(Seq(a -> b), color)
  }

  def lines(dashes: Seq[(Vector2, Vector2)], color: Color = null): Unit = {
    shape(LINE, color) {
      for ((p1, p2) <- dashes) {
        shapeRenderer.line(p1, p2)
      }
    }
  }

  def lines(dashSets: Seq[(Vector2, Vector2)]*): Unit = {
    shape(LINE) {
      for {
        dashes <- dashSets
        (p1, p2) <- dashes
      } {
        shapeRenderer.line(p1, p2)
      }
    }
  }

  def scissor(at: Vec2 = Vec2(0,0), size: Vec2 = Vec2(2,2))(f: => Unit): Unit = {
    val rIn = new Rectangle((at.x - size.x / 2.0).toFloat, (at.y - size.y / 2.0).toFloat, size.x.toFloat, size.y.toFloat)
    val rOut = new Rectangle()
    ScissorStack.calculateScissors(camera, transform.current, rIn, rOut)
    ScissorStack.pushScissors(rOut)
    try {
      f
    } finally {
      ScissorStack.popScissors()
    }
  }

  def viewport[_: Projection](viewportSize: Double,
                              heading: Float = 0.0f,
                              offs: Vector2 = Vector2.Zero)
                             (f: => Unit) = {
    projection.viewport(viewportSize, heading, offs)(f)
  }

  def prePosition[_: Projection](from: Vec2): Vec2 = {
    projection.prePosition(from)
  }

  def prePosition[_: Projection](from: Vec3): Vec3 = {
    projection.prePosition(from)
  }

  def at[_: Projection](position: Vec3, heading: Double)(f: => Unit): Unit = {
    projection.at(position, heading)(f)
  }

  def at[_: Projection](position: Vec3)(f: => Unit): Unit = {
    at(position, heading = 0.0)(f)
  }

  def at[_: Projection](position: Vec2)(f: => Unit): Unit = {
    projection.at(position, heading = 0.0)(f)
  }

  def at[_: Projection](position: Vec2, heading: Double)(f: => Unit): Unit = {
    projection.at(position, heading)(f)
  }

  def rotatedTo[_: Projection](heading: Double)(f: => Unit): Unit = {
    projection.rotatedTo(heading)(f)
  }

  def screen2World[_: Projection]: Float = {
    projection.screen2World
  }

  def projection[_: Projection]: Projection[_] = implicitly[Projection[_]]

}

trait Projection[+T] {
  def viewport(viewportSize: Double, heading: Float = 0.0f, offs: Vector2 = Vector2.Zero)(f: => Unit)
  def at(position: Vec3, heading: Double)(f: => Unit): Unit
  def at(position: Vec2, heading: Double)(f: => Unit): Unit
  def prePosition(from: Vec2): Vec2
  def prePosition(from: Vec3): Vec3
  def rotatedTo(heading: Double)(f: => Unit): Unit
  def screen2World: Float
  def headingCorrection: Float
}

case class ScreenProjection() extends Projection[Any] {

  val transform = RenderContext.transform

  override def viewport(viewportSize: Double, heading: Float, offs: Vector2)(f: => Unit): Unit = {
    f
  }

  override def screen2World: Float = {
    1.0f / transform.current.getScaleX
  }

  override def rotatedTo(heading: Double)(f: => Unit): Unit = {
    transform(_
      .rotate(-heading)) {
      f
    }
  }

  def headingCorrection: Float = {
    0.0f
  }

  def prePosition(from: Vec2): Vec2 = {
    from
  }

  def prePosition(from: Vec3): Vec3 = {
    from
  }

  override def at(position: Vec2, heading: Double)(f: => Unit): Unit = {
    at(Vec3(position.x, position.y), heading)(f)
  }

  override def at(position: Vec3, heading: Double)(f: => Unit): Unit = {

    val w = Gdx.graphics.getWidth.toFloat
    val h = Gdx.graphics.getHeight.toFloat

    val (sx, sy) = if (w > h) {
      (w / h, 1.0f)
    } else {
      (1.0f, h / w)
    }

    val delta = Vec2(position.x * sx, position.y * sy)

    transform(_.translate(delta)) {
      f
    }
  }

}

case class PpiProjection() extends Projection[Any] {

  val transform = RenderContext.transform

  def viewport(viewportSize: Double, heading: Float = 0.0f, offs: Vector2 = Vector2.Zero)(f: => Unit) {
    transform(_
      .scalexy(2.0f / viewportSize.toFloat)
      .translate(offs.x, offs.y, 0.0f)
      .rotate(heading)) {
      f
    }
  }

  override def at(position: Vec2, heading: Double)(f: => Unit): Unit = {
    at(Vec3(position.x, position.y), heading)(f)
  }

  def at(position: Vec3, heading: Double = 0.0)(f: => Unit): Unit = {
    transform(_
      .translate(position - self.position : Vec2)
      .rotate(-heading)) {
      f
    }
  }

  def prePosition(from: Vec2): Vec2 = {
    from - self.position
  }

  def prePosition(from: Vec3): Vec3 = {
    from - self.position
  }

  def rotatedTo(heading: Double)(f: => Unit): Unit = {
    transform(_
      .rotate(-heading)) {
      f
    }
  }

  def screen2World: Float = {
    1.0f / math.sqrt(transform.current.getScaleXSquared).toFloat
  }

  def headingCorrection: Float = {
    -self.heading
  }
}

case class BScopeProjection(widthDegrees: Double, use3dDist: Boolean) extends Projection[Any] {

  val transform = RenderContext.transform
  var viewportSize = 2.0

  def viewport(viewportSize: Double, heading: Float = 0.0f, offs: Vector2 = Vector2.Zero)(f: => Unit) {
    val prev = viewportSize
    this.viewportSize = viewportSize
    transform(_
      .scalexy(2.0f / viewportSize.toFloat)
      .translate(offs.x, offs.y, 0.0f)) {
      f
    }
    this.viewportSize = prev
  }

  def prePosition(position: Vec2): Vec2 = {
    prePosition(Vec3(position.x, position.y, self.position.z))
  }

  def prePosition(position: Vec3): Vec3 = {
    val bra = if (use3dDist) (position - self.position: Vec3).asBra else (position - self.position: Vec2).asBra
    val relAz = NormalizeDegrees.pm180(bra.bearing - self.heading)
    val range = bra.range3d

    val x = azToX(relAz)
    val y = range - 0.5 * viewportSize

    Vec3(x,y,0.0)
  }

  def at(position: Vec2, heading: Double)(f: => Unit): Unit = {
    at(Vec3(position.x, position.y, self.position.z), heading)(f)
  }

  def at(position: Vec3, heading: Double = 0.0)(f: => Unit): Unit = {
    val bra = if (use3dDist) (position - self.position: Vec3).asBra else (position - self.position: Vec2).asBra
    val relAz = NormalizeDegrees.pm180(bra.bearing - self.heading)
    val range = bra.range3d

    val x = azToX(relAz)
    val y = range - 0.5 * viewportSize

    transform(_
      .translate(x.toFloat, y.toFloat)
      .rotate(self.heading-heading)) {
      f
    }
  }

  def rotatedTo(heading: Double)(f: => Unit): Unit = {
    transform(_.rotate(-heading)) {
      f
    }
  }

  def screen2World: Float = {
    1.0f / math.sqrt(transform.current.getScaleXSquared).toFloat
  }

  def headingCorrection: Float = {
    -self.heading
  }

  def azToX(azDegrees: Double): Double = {
    viewportSize * azDegrees / widthDegrees
  }

}