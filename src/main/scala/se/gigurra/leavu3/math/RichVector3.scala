package se.gigurra.leavu3.math

import com.badlogic.gdx.math.{Vector2, Vector3}

import scala.language.implicitConversions

trait RichVector3Implicits {

  implicit class VectorOps(v: Vector3) {
    def +(delta: Float): Vector3 = new Vector3(v.x + delta, v.y + delta, v.z + delta)
    def -(delta: Float): Vector3 = new Vector3(v.x - delta, v.y - delta, v.z - delta)
    def *(c: Float): Vector3 = new Vector3(v.x * c, v.y * c, v.z * c)
    def /(d: Float): Vector3 = new Vector3(v.x / d, v.y / d, v.z / d)
    def unary_- : Vector3 = new Vector3(-v.x, -v.y, -v.z)
    def norm: Float = v.len()
    def normalized: Vector3 = v / norm
    def +(b: Vector3): Vector3 = new Vector3(v.x + b.x, v.y + b.y, v.z + b.z)
    def -(b: Vector3): Vector3 = new Vector3(v.x - b.x, v.y - b.y, v.z - b.z)
    def ***(b: Vector3): Vector3 = new Vector3(v.x * b.x, v.y * b.y, v.z * b.z)
    def /\/(b: Vector3): Vector3 = new Vector3(v.x / b.x, v.y / b.y, v.z / b.z)
    def dot(b: Vector3): Float = v.x * b.x + v.y * b.y + v.z * b.z
    def cross(b: Vector3): Vector3 = new Vector3(v).crs(b)
    def v2: Vector2 = v32v2(v)
    def xy: Vector2 = v32v2(v)
  }

  implicit class ConstVectorOps(c: Float) {
    def +(v: Vector3): Vector3 = v + c
    def -(v: Vector3): Vector3 = -v + c
    def *(v: Vector3): Vector3 = v * c
    def /(v: Vector3): Vector3 = new Vector3(c / v.x, c / v.y, c / v.z)
  }

  implicit class Vector2Ops(v: Vector2) {
    def +(delta: Float): Vector2 = new Vector2(v.x + delta, v.y + delta)
    def -(delta: Float): Vector2 = new Vector2(v.x - delta, v.y - delta)
    def *(c: Float): Vector2 = new Vector2(v.x * c, v.y * c)
    def /(d: Float): Vector2 = new Vector2(v.x / d, v.y / d)
    def unary_- : Vector2 = new Vector2(-v.x, -v.y)
    def norm: Float = v.len()
    def normalized: Vector2 = v / norm
    def +(b: Vector2): Vector2 = new Vector2(v.x + b.x, v.y + b.y)
    def -(b: Vector2): Vector2 = new Vector2(v.x - b.x, v.y - b.y)
    def ***(b: Vector2): Vector2 = new Vector2(v.x * b.x, v.y * b.y)
    def /\/(b: Vector2): Vector2 = new Vector2(v.x / b.x, v.y / b.y)
    def dot(b: Vector2): Float = v.x * b.x + v.y * b.y
  }

  implicit class ConstVector2Ops(c: Float) {
    def +(v: Vector2): Vector2 = v + c
    def -(v: Vector2): Vector2 = -v + c
    def *(v: Vector2): Vector2 = v * c
    def /(v: Vector2): Vector2 = new Vector2(c / v.x, c / v.y)
  }

  implicit def tuple32V3(t: (Float, Float, Float)): Vector3 = new Vector3(t._1, t._2, t._3)
  implicit def tuple22V2(t: (Float, Float)): Vector2 = new Vector2(t._1, t._2)

  implicit def tuplev32tuplev2(t: (Vector3, Vector3)): (Vector2, Vector2)= (v32v2(t._1), v32v2(t._2))

  implicit def v32v2(v3: Vector3): Vector2 = new Vector2(v3.x, v3.y)
  implicit def v22v3(v2: Vector3): Vector3 = new Vector3(v2.x, v2.y, 0.0f)

}

