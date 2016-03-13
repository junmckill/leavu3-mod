package se.gigurra.leavu3.math

import com.badlogic.gdx.math.{Vector2, Vector3}
import se.gigurra.leavu3.externaldata.{Vec2, Vec3}

import scala.language.implicitConversions

trait RichVector3Implicits {

  implicit class ConstVecOps(c: Double) {
    def +(v: Vec3): Vec3 = v + c
    def -(v: Vec3): Vec3 = -v + c
    def *(v: Vec3): Vec3 = v * c
    def /(v: Vec3): Vec3 = new Vec3(c / v.x, c / v.y, c / v.z)
  }

  implicit class ConstVec2Ops(c: Double) {
    def +(v: Vec2): Vec2 = v + c
    def -(v: Vec2): Vec2 = -v + c
    def *(v: Vec2): Vec2 = v * c
    def /(v: Vec2): Vec2 = new Vec2(c / v.x, c / v.y)
  }

  implicit def tuple32V3(t: (Double, Double, Double)): Vec3 = new Vec3(t._1, t._2, t._3)
  implicit def tuple22V2(t: (Double, Double)): Vec2 = new Vec2(t._1, t._2)

  implicit def tuplev32tuplev2(t: (Vec3, Vec3)): (Vec2, Vec2)= (t._1, t._2)
  implicit def tuplev32tuplev2v3(t: (Vec3, Vec3)): (Vector3, Vector3)= (t._1, t._2)
  implicit def tuplev32tuplev2v33(t: (Vec3, Vec3)): (Vector2, Vector2)= (t._1, t._2)
  implicit def tuplev32tuplev2v2(t: (Vec2, Vec2)): (Vector2, Vector2)= (t._1, t._2)

  implicit def tuplevSeq32tuplev2v2(t: Seq[(Vec2, Vec2)]): Seq[(Vector2, Vector2)]= t.map(tuplev32tuplev2v2)
  implicit def tuplevSeq32tuplev2v23(t: Seq[(Vec3, Vec3)]): Seq[(Vector2, Vector2)]= t.map(tuplev32tuplev2v33)

  implicit def vector22vector3(v: Vector2): Vector3 = new Vector3(v.x, v.y, 0.0f)
  implicit def vector32vector2(v: Vector3): Vector2 = new Vector2(v.x, v.y)

  implicit class RichTupleVec2(vv: (Vec2, Vec2)) {
    def *(const: Double): (Vec2, Vec2) = (vv._1 * const, vv._2 * const)
    def /(const: Double): (Vec2, Vec2) = (vv._1 / const, vv._2 / const)
    def +(const: Double): (Vec2, Vec2) = (vv._1 + const, vv._2 + const)
    def +(const: Vec2): (Vec2, Vec2) = (vv._1 + const, vv._2 + const)
    def -(const: Double): (Vec2, Vec2) = (vv._1 - const, vv._2 - const)
  }

  implicit class RichTupleSeqVec2(vvs: Seq[(Vec2, Vec2)]) {
    def *(const: Double): Seq[(Vec2, Vec2)] = vvs.map(_*const)
    def /(const: Double): Seq[(Vec2, Vec2)] = vvs.map(_/const)
    def +(const: Double): Seq[(Vec2, Vec2)] = vvs.map(_+const)
    def +(const: Vec2): Seq[(Vec2, Vec2)] = vvs.map(_+const)
    def -(const: Double): Seq[(Vec2, Vec2)] = vvs.map(_-const)
  }
}

