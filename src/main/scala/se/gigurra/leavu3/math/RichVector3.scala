package se.gigurra.leavu3.math

import com.badlogic.gdx.math.Vector3

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
  }

  implicit class ConstVectorOps(c: Float) {
    def +(v: Vector3): Vector3 = v + c
    def -(v: Vector3): Vector3 = -v + c
    def *(v: Vector3): Vector3 = v * c
    def /(v: Vector3): Vector3 = new Vector3(c / v.x, c / v.y, c / v.z)
  }

  implicit def tuple22V3(t: (Float, Float)): Vector3 = new Vector3(t._1, t._2, 0.0f)
  implicit def tuple32V3(t: (Float, Float, Float)): Vector3 = new Vector3(t._1, t._2, t._3)

}

