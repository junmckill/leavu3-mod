package se.gigurra.leavu3.mfd

import se.gigurra.leavu3.externaldata.Vec2

object shapes {

  object hsi {

    val w = 0.025
    val h = 0.075

    val flag = Seq(
      Vec2(0.00,  0.00) -> Vec2(0.00,     h),
      Vec2(0.00,     h) -> Vec2(  -w, h-w/2),
      Vec2(-w,   h-w/2) -> Vec2(0.00,   h-w)
    )

    val eastPin  = Seq(Vec2(0.00,  0.00) -> Vec2( h/2, 0.00))
    val westPin  = Seq(Vec2(0.00,  0.00) -> Vec2(-h/2, 0.00))
    val southPin = Seq(Vec2(0.00,  0.00) -> Vec2(0.00, -h/2))
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
