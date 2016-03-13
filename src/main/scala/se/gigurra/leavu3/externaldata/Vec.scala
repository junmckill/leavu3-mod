package se.gigurra.leavu3.externaldata

import com.badlogic.gdx.math.{Vector2, Vector3}
import scala.language.implicitConversions

case class Bra(bearing: Double, range: Double, deltaAltitude: Double) {
  def toOffset: Vec3 = {
    val scale = math.cos(deltaAltitude / range)
    val y = range * math.cos(bearing.toRadians) * scale
    val x = range * math.sin(bearing.toRadians) * scale
    Vec3(x, y, deltaAltitude)
  }
}

case class Vec3(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0) {
  def +(delta: Double): Vec3 = new Vec3(this.x + delta, this.y + delta, this.z + delta)
  def -(delta: Double): Vec3 = new Vec3(this.x - delta, this.y - delta, this.z - delta)
  def *(c: Double): Vec3 = new Vec3(this.x * c, this.y * c, this.z * c)
  def /(d: Double): Vec3 = new Vec3(this.x / d, this.y / d, this.z / d)
  def unary_- : Vec3 = new Vec3(-this.x, -this.y, -this.z)
  def norm: Double = Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z)
  def normalized: Vec3 = this / norm
  def +(b: Vec3): Vec3 = new Vec3(this.x + b.x, this.y + b.y, this.z + b.z)
  def -(b: Vec3): Vec3 = new Vec3(this.x - b.x, this.y - b.y, this.z - b.z)
  def ***(b: Vec3): Vec3 = new Vec3(this.x * b.x, this.y * b.y, this.z * b.z)
  def /\/(b: Vec3): Vec3 = new Vec3(this.x / b.x, this.y / b.y, this.z / b.z)
  def dot(b: Vec3): Double = this.x * b.x + this.y * b.y + this.z * b.z
  def cross(b: Vec3): Vec3 = Vec3(this.y*b.z - this.z*b.y, this.z*b.x - this.x - b.z, this.x*b.y - this.y*b.z)
  def vec2: Vec2 = Vec2(x,y)
  def withZeroZ: Vec3 = Vec3(x,y,0.0)
  def asBra: Bra = {
    val bearing = math.atan2(x, y).toDegrees
    val range = norm
    Bra(bearing, range, z)
  }
}

case class Vec2(x: Double = 0.0, y: Double = 0.0) {
  def +(delta: Double): Vec2 = new Vec2(this.x + delta, this.y + delta)
  def -(delta: Double): Vec2 = new Vec2(this.x - delta, this.y - delta)
  def *(c: Double): Vec2 = new Vec2(this.x * c, this.y * c)
  def /(d: Double): Vec2 = new Vec2(this.x / d, this.y / d)
  def unary_- : Vec2 = new Vec2(-this.x, -this.y)
  def norm: Double = Math.sqrt(this.x * this.x + this.y * this.y)
  def normalized: Vec2 = this / norm
  def +(b: Vec2): Vec2 = new Vec2(this.x + b.x, this.y + b.y)
  def -(b: Vec2): Vec2 = new Vec2(this.x - b.x, this.y - b.y)
  def ***(b: Vec2): Vec2 = new Vec2(this.x * b.x, this.y * b.y)
  def /\/(b: Vec2): Vec2 = new Vec2(this.x / b.x, this.y / b.y)
  def dot(b: Vec2): Double = this.x * b.x + this.y * b.y
  def asBra: Bra = {
    val bearing = math.atan2(x, y).toDegrees
    val range = norm
    Bra(bearing, range, 0.0)
  }
}

object Vec3 {
  implicit def v32gdxv3(a: Vec3): Vector3 = new Vector3(a.x.toFloat, a.y.toFloat, a.z.toFloat)
  implicit def v32gdxv2(a: Vec3): Vector2 = new Vector2(a.x.toFloat, a.y.toFloat)
  implicit def v322(a: Vec3): Vec2 = a.vec2
}

object Vec2 {
  implicit def v22gdxv2(a: Vec2): Vector2 = new Vector2(a.x.toFloat, a.y.toFloat)
  implicit def v22gdxv3(a: Vec2): Vector3 = new Vector3(a.x.toFloat, a.y.toFloat, 0.0f)
}
