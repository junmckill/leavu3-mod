package se.gigurra.leavu3.mfd

import se.gigurra.leavu3.externaldata.Vec2

object shapes {
  object hsi {
    object flag {
      val w = 0.025
      val h = 0.075
      val coords = Seq(
        Vec2(0.00,  0.00) -> Vec2(0.00,     h),
        Vec2(0.00,     h) -> Vec2(  -w, h-w/2),
        Vec2(-w,   h-w/2) -> Vec2(0.00,   h-w)
      )
    }
  }
}
