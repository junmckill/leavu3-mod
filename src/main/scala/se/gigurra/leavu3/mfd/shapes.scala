package se.gigurra.leavu3.mfd

import se.gigurra.leavu3.datamodel.Vec2
import se.gigurra.leavu3.gfx.RenderContext._

object shapes {

  object hsi {

    val w = 0.025
    val h = 0.075

    val flag = Seq(
      Vec2(0.00,   0.00) -> Vec2(0.00,     -h),
      Vec2(0.00,     -h) -> Vec2(  -w, -h+w/2),
      Vec2(-w,   -h+w/2) -> Vec2(0.00,   -h+w)
    )

    val eastPin  = Seq(Vec2(0.00,  0.00) -> Vec2(-h/2, 0.00))
    val westPin  = Seq(Vec2(0.00,  0.00) -> Vec2(+h/2, 0.00))
    val southPin = Seq(Vec2(0.00,  0.00) -> Vec2(0.00, +h/2))

    def detail(radius: Float) : Seq[(Vec2, Vec2)] = {
      val r0 = radius.toDouble
      val r1 = r0 -  radius * h / 2.0
      val n = 36
      for (i <- 0 until n) yield {
        val angle = (360.0 * i.toDouble / n.toDouble).toRadians
        val a = r0 * Vec2(math.cos(angle), math.sin(angle))
        val b = r1 * Vec2(math.cos(angle), math.sin(angle))
        a -> b
      }
    }
  }



  object self {

    val h = 0.075
    val dx1 = 0.015
    val dx2 = 0.03

    val coords = Seq(
      Vec2(0.00,  -h/2) -> Vec2(0.00,   h/2),
      Vec2(-dx1,  -h/4) -> Vec2( dx1,  -h/4),
      Vec2(-dx2,   h/4) -> Vec2( dx2,   h/4)
    )
  }

}
